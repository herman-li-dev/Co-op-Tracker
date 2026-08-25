import { expect, test } from '@playwright/test';

test('user can create an application, advance it to interview, and see dashboard updates', async ({
  page,
}) => {
  const uniqueId = `${Date.now()}-${Math.floor(Math.random() * 10_000)}`;
  const account = `e2e-${uniqueId}`;
  const password = `E2e-${uniqueId}-Pass!`;
  const company = `Northstar E2E ${uniqueId}`;

  await page.goto('/user/register');
  await page.getByPlaceholder('Account').fill(account);
  await page.getByPlaceholder('Password', { exact: true }).fill(password);
  await page.getByPlaceholder('Confirm password').fill(password);
  await page.getByRole('button', { name: 'Create account' }).click();

  await expect(page).toHaveURL(/\/user\/login/);
  await page.getByPlaceholder('Account').fill(account);
  await page.getByPlaceholder('Password').fill(password);
  await page.getByRole('button', { name: /sign in|login/i }).click();

  await expect(page).toHaveURL(/\/welcome/);
  await page.getByRole('button', { name: 'Manage applications' }).click();
  await expect(page).toHaveURL(/\/applications/);

  await page.getByRole('button', { name: 'Add application' }).click();
  const dialog = page.locator('.ant-modal:visible');
  await expect(dialog).toContainText('Add application');
  await dialog.getByLabel('Company').fill(company);
  await dialog.getByLabel('Job title').fill('Software Developer Intern');
  await dialog.getByRole('button', { name: 'Add', exact: true }).click();

  await expect(page.getByText('Application added')).toBeVisible();
  const applicationRow = page.getByRole('row').filter({ hasText: company });
  await expect(applicationRow).toContainText('Saved');

  await applicationRow.locator('.ant-select-selector').click();
  await page.getByRole('option', { name: 'Interview', exact: true }).click();
  await expect(page.getByText('Status updated')).toBeVisible();
  await expect(applicationRow).toContainText('Interview');

  await page.goto('/welcome');
  const interviewStatistic = page.locator('.ant-statistic').filter({ hasText: 'Interviews' });
  await expect(interviewStatistic).toContainText('1');

  const recentApplication = page.getByRole('row').filter({ hasText: company });
  await expect(recentApplication).toContainText('Software Developer Intern');
  await expect(recentApplication).toContainText('Interview');
});
