package com.herman.usercenter.controller;

import com.herman.usercenter.model.domain.User;
import com.herman.usercenter.model.domain.request.JobApplicationAddRequest;
import com.herman.usercenter.model.enums.JobApplicationStatus;
import com.herman.usercenter.model.vo.ApplicationDashboardVO;
import com.herman.usercenter.model.vo.ApplicationStatusCountVO;
import com.herman.usercenter.service.JobApplicationService;
import com.herman.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static com.herman.usercenter.contant.UserConstant.USER_LOGIN_STATE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JobApplicationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JobApplicationService applicationService;

    @Test
    void dashboardAggregatesOnlyTheSignedInUsersApplications() {
        User owner = createUser("dashboard-owner");
        User anotherUser = createUser("dashboard-other");
        LocalDate today = LocalDate.now();

        addApplication(owner, "Alpha", JobApplicationStatus.APPLIED,
                today.minusDays(10), today.plusDays(3), today.minusDays(1));
        addApplication(owner, "Bravo", JobApplicationStatus.INTERVIEW,
                today.minusDays(3), today.plusDays(8), today.plusDays(1));
        addApplication(owner, "Charlie", JobApplicationStatus.OFFER,
                today, today.plusDays(7), today);
        addApplication(owner, "Delta", JobApplicationStatus.REJECTED,
                today.minusWeeks(10), today.minusDays(1), null);

        addApplication(anotherUser, "Other user company", JobApplicationStatus.INTERVIEW,
                today, today.plusDays(2), today.minusDays(1));

        ApplicationDashboardVO dashboard = applicationService.getDashboard(owner.getId());

        assertEquals(4L, dashboard.getTotal());
        assertEquals(1L, dashboard.getApplied());
        assertEquals(1L, dashboard.getInterviews());
        assertEquals(1L, dashboard.getOffers());
        assertEquals(1L, dashboard.getRejected());
        assertEquals(new BigDecimal("25.0"), dashboard.getInterviewRate());
        assertEquals(new BigDecimal("25.0"), dashboard.getOfferRate());
        assertEquals(2L, dashboard.getUpcomingDeadlines());
        assertEquals(2L, dashboard.getFollowUpsDue());
        assertEquals(4, dashboard.getRecentApplications().size());
        assertTrue(dashboard.getRecentApplications().stream()
                .allMatch(application -> owner.getId().equals(application.getUserId())));
        assertFalse(dashboard.getRecentApplications().stream()
                .anyMatch(application -> "Other user company".equals(application.getCompanyName())));

        Map<String, Long> statusCounts = dashboard.getStatusDistribution().stream()
                .collect(Collectors.toMap(
                        ApplicationStatusCountVO::getStatus,
                        ApplicationStatusCountVO::getCount));
        assertEquals(1L, statusCounts.get(JobApplicationStatus.APPLIED.name()));
        assertEquals(1L, statusCounts.get(JobApplicationStatus.INTERVIEW.name()));
        assertEquals(1L, statusCounts.get(JobApplicationStatus.OFFER.name()));
        assertEquals(1L, statusCounts.get(JobApplicationStatus.REJECTED.name()));
        assertEquals(8, dashboard.getWeeklyTrend().size());
        assertEquals(3L, dashboard.getWeeklyTrend().stream()
                .mapToLong(item -> item.getCount()).sum());
    }

    @Test
    void applicationEndpointsRequireLoginAndHideAnotherUsersApplication() throws Exception {
        User owner = createUser("permission-owner");
        User anotherUser = createUser("permission-other");
        long applicationId = addApplication(owner, "Private Company", JobApplicationStatus.APPLIED,
                LocalDate.now(), LocalDate.now().plusDays(5), null);
        MockHttpSession anotherUsersSession = signedInSession(anotherUser);

        mockMvc.perform(get("/application/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40100));

        mockMvc.perform(get("/application/get")
                        .param("id", String.valueOf(applicationId))
                        .session(anotherUsersSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));

        mockMvc.perform(post("/application/update")
                        .session(anotherUsersSession)
                        .contentType("application/json")
                        .content("{\"id\":" + applicationId + ",\"status\":\"OFFER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));

        mockMvc.perform(post("/application/delete")
                        .session(anotherUsersSession)
                        .contentType("application/json")
                        .content("{\"id\":" + applicationId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));

        assertEquals(JobApplicationStatus.APPLIED.name(),
                applicationService.getApplication(applicationId, owner.getId()).getStatus());
    }

    @Test
    void listEndpointFiltersSortsAndReturnsOnlyOwnedApplications() throws Exception {
        User owner = createUser("filter-owner");
        User anotherUser = createUser("filter-other");
        LocalDate today = LocalDate.now();

        addApplication(owner, "Zulu", JobApplicationStatus.APPLIED,
                today.minusDays(4), today.plusDays(5), null);
        addApplication(owner, "Alpha", JobApplicationStatus.APPLIED,
                today.minusDays(2), today.plusDays(7), null);
        addApplication(owner, "Outside range", JobApplicationStatus.APPLIED,
                today.minusDays(20), today.plusDays(10), null);
        addApplication(anotherUser, "Another user's company", JobApplicationStatus.APPLIED,
                today.minusDays(3), today.plusDays(6), null);

        mockMvc.perform(get("/application/list")
                        .session(signedInSession(owner))
                        .param("status", "APPLIED")
                        .param("appliedDateStart", today.minusDays(5).toString())
                        .param("appliedDateEnd", today.toString())
                        .param("sortField", "companyName")
                        .param("sortOrder", "ascend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[*].companyName", contains("Alpha", "Zulu")))
                .andExpect(jsonPath("$.data.records[*].userId", everyItem(is(owner.getId().intValue()))));
    }

    private User createUser(String prefix) {
        User user = new User();
        user.setUserName(prefix);
        user.setUserAccount(prefix + "-" + System.nanoTime());
        user.setUserPassword("test-password-hash");
        user.setUserStatus(0);
        user.setUserRole(0);
        user.setIsDelete(0);
        assertTrue(userService.save(user));
        return user;
    }

    private long addApplication(
            User owner,
            String company,
            JobApplicationStatus status,
            LocalDate appliedDate,
            LocalDate deadline,
            LocalDate nextFollowUpDate) {
        JobApplicationAddRequest request = new JobApplicationAddRequest();
        request.setCompanyName(company);
        request.setJobTitle("Software Developer Intern");
        request.setStatus(status.name());
        request.setAppliedDate(appliedDate);
        request.setDeadline(deadline);
        request.setNextFollowUpDate(nextFollowUpDate);
        return applicationService.addApplication(request, owner.getId());
    }

    private MockHttpSession signedInSession(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(USER_LOGIN_STATE, user);
        return session;
    }
}
