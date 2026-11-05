// Export an Archify diagram's light and dark SVGs (the README embeds both).
// usage: node export.cjs interactive/<name>.html   -> <name>-light.svg, <name>-dark.svg
// needs: npm i playwright && npx playwright install chromium
const { chromium } = require('playwright');
const path = require('path');
(async () => {
  const html = path.resolve(process.argv[2]);
  const name = path.basename(html, '.html');
  const browser = await chromium.launch();
  for (const theme of ['light', 'dark']) {
    const ctx = await browser.newContext({ acceptDownloads: true, viewport: { width: 1440, height: 900 }, colorScheme: theme });
    const page = await ctx.newPage();
    await page.goto('file://' + html + '?theme=' + theme);
    await page.waitForTimeout(800);
    await page.getByRole('button', { name: /export/i }).first().click();
    const [download] = await Promise.all([page.waitForEvent('download'), page.click(`button[data-format="svg-${theme}"]`)]);
    await download.saveAs(path.join(__dirname, `${name}-${theme}.svg`));
    console.log(`${name}-${theme}.svg`);
    await ctx.close();
  }
  await browser.close();
})();
