package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Pagina en la que se ordenan los productos en orden alfabetico
 * luego de que una marca haya sido elegida en la pagina de marcas
 */
public class BrandSortPage extends PageObject {

    @FindBy(css = "#sortBy")
    private WebElementFacade sortDropdown; // Dropdown de ordenamiento

    @FindBy(css = ".product-card")
    private List<WebElementFacade> products; // Tarjetas de productos visibles en la pagina

    // Metodo para abrir el dropdown de ordenamiento
    public void clickSortDropdown() {
        try {
            sortDropdown.waitUntilVisible().withTimeoutOf(Duration.ofSeconds(10));
            sortDropdown.waitUntilClickable().withTimeoutOf(Duration.ofSeconds(5));

            try {
                sortDropdown.click();
            } catch (Exception ignored) {
                JavascriptExecutor js = (JavascriptExecutor) getDriver();
                js.executeScript("arguments[0].click();", sortDropdown);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo abrir el dropdown de ordenamiento", e);
        }
    }

    // Metodo para seleccionar la opcion de ordenamiento A-Z
    public void selectSortOptionAZ() {
        try {
            sortDropdown.waitUntilVisible().withTimeoutOf(Duration.ofSeconds(10));

            Select select = new Select(sortDropdown);
            select.selectByValue("title-ascending");

            // Despachar un evento de cambio para asegurar que el sitio reconozca la seleccion del dropdown
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            js.executeScript(
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                sortDropdown
            );

            waitForSortToApply();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo seleccionar la opcion A-Z", e);
        }
    }

    // Metodo para esperar a que el ordenamiento A-Z se aplique
    private void waitForSortToApply() {
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(15).toMillis();

        while (System.currentTimeMillis() < deadline) {
            String currentUrl = getDriver().getCurrentUrl();

            // Verificar que la URL contiene el parametro de ordenamiento A-Z
            if (currentUrl.contains("sort_by=title-ascending")) {
                waitForProductsToLoad();
                return;
            }
            waitABit(250);
        }

        throw new RuntimeException("El ordenamiento A-Z no se aplico (URL sin sort_by=title-ascending)");
    }

    // Metodo para obtener los nombres de todos los productos visibles en la pagina
    public List<String> getAllProductNamesOnPage() {
        List<String> productNames = new ArrayList<>();

        waitForRenderedElementsToBePresent(By.cssSelector(".product-card"));
        waitABit(1000);

        List<WebElementFacade> productCards = products;

        for (WebElementFacade product : productCards) {
            try {
                String name = product.findBy(By.tagName("p")).getText().trim();
                if (!name.isEmpty()) {
                    productNames.add(name);
                }
            } catch (Exception ignored) {
                // Saltarse cualquier tarjeta de producto que no tenga el formato esperado
            }
        }

        return productNames;
    }

    // Metodo para obtener todos los productos visibles en la pagina
    public List<WebElementFacade> getAllVisibleProducts() {
        waitForRenderedElementsToBePresent(By.cssSelector(".product-card"));
        return products;
    }

    // Metodo para obtener la cantidad de productos visibles en la pagina
    public int getProductCount() {
        return getAllVisibleProducts().size();
    }

    // Metodo para esperar a que los productos se carguen despues de aplicar el ordenamiento
    public void waitForProductsToLoad() {
        waitForRenderedElementsToBePresent(By.cssSelector(".product-card"));
        waitABit(1000);
    }

    // Metodo alternativo a waitABit 
    public void pause(long milliseconds) {
        waitABit(milliseconds);
    }
}
