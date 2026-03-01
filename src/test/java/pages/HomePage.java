package pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.FindBy;
import java.util.List;

//se aconseja tener variabes estáticas o una clase de selectotes para evitar hardcodear los selectores en los métodos

@DefaultUrl("https://www.thecubicle.com/en-global") // Sitio principal
public class HomePage extends PageObject {

    @FindBy(css = ".product-card")
    private List<WebElementFacade> products; // Lista de productos visibles en la página principal  

    // Todos los productos disponibles a seleccionar
    public List<WebElementFacade> getAllVisibleProducts() {
        waitForRenderedElementsToBePresent(By.cssSelector(".product-card"));
        return products;
    }

    // Método para encontrar un producto específico por su nombre
    public WebElementFacade findProductCardByName(String productName) {
        List<WebElementFacade> currentProducts = findAll(".product-card"); // Refresca la lista de productos visibles
        String expected = productName.trim().toLowerCase(); // Normaliza el nombre del producto para comparación

        // Itera sobre los productos visibles para encontrar el que coincida con el nombre esperado
        for (WebElementFacade product : currentProducts) {
            try {
                String currentName = product.findBy(By.tagName("p")).getText().trim().toLowerCase();
                if (currentName.equals(expected) || currentName.contains(expected) || expected.contains(currentName)) {
                    return product;
                }
            } catch (Exception ignored) {
                // Seguir con el siguiente producto
            }
        }

        return null;
    }

    // Método para agregar un producto al carrito desde su tarjeta
    public void addProductToCart(WebElementFacade product) {
        scrollToElement(product); // Bajar al elemento hasta que sea visible
        waitABit(500); // Tiempo de espera de medio segundo

        WebElementFacade addButton = product.findBy(By.cssSelector("button[title='Add this product to the cart']")); // Elemento del botón "Add to Cart"
        addButton.waitUntilClickable(); // Esperar a que se pueda dar click al boton

        try {
            addButton.click();
        } catch (Exception ignored) {
            // Si el click normal falla, usar JavaScript para hacer click
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            js.executeScript("arguments[0].click();", addButton);
        }

        waitABit(800);
    }

    // Metodo para bajar o subir a algun elemento que no es visible en pantalla
    private void scrollToElement(WebElementFacade element) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    // Metodo alternativo a waitABit para pausar la ejecución por un tiempo específico
    public void pause(long milliseconds) {
        waitABit(milliseconds);
    }
}
