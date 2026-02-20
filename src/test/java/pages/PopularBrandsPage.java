package pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.FindBy;
import java.util.List;

@DefaultUrl("https://www.thecubicle.com/en-global/pages/collections/top-brands") // URL de la pagina de marcas populares
public class PopularBrandsPage extends PageObject {

    @FindBy(css = "#shopify-section-template--19037914595411__main a")
    private List<WebElementFacade> brandCards; // Tarjetas de marcas visibles en la pagina

    // Metodo para navegar a la pagina de marcas populares
    public void navigateToPopularBrands() {
        getDriver().get("https://www.thecubicle.com/en-global/pages/collections/top-brands");
        waitForPageToLoad();
    }

    // Metodo para esperar a que la pagina de marcas populares cargue completamente
    public void waitForPageToLoad() {
        waitABit(2000);
        
        try {
            // Esperar a que las marcas sean visibles en la pagina, lo que indica que la pagina ha cargado correctamente
            waitForRenderedElementsToBePresent(By.cssSelector("#shopify-section-template--19037914595411__main a"));
            waitABit(1000);
        } catch (Exception e) {
            throw new RuntimeException("Popular Brands page no cargó correctamente", e);
        }
    }

    // Metodo para obtener todas las marcas visibles en la pagina
    public List<WebElementFacade> getAllBrands() {
        
        // Asegurarse de que las marcas estén presentes antes de intentar obtenerlas
        waitForRenderedElementsToBePresent(By.cssSelector("#shopify-section-template--19037914595411__main a"));
        
        // Verificar que se encontraron marcas en la pagina
        if (brandCards.isEmpty()) {
            throw new RuntimeException("No se encontraron marcas en la página");
        }
        
        return brandCards;
    }

    // Metodo para obtener la cantidad de marcas visibles en la pagina
    public int getBrandCount() {
        return getAllBrands().size();
    }

    /**
     * Metodo para hacer clic en una marca especifica
     * @param brandCard La tarjeta de la marca en la que se desea hacer clic, obtenida a través del metodo getAllBrands()
     * Este metodo utiliza JavaScript para hacer clic en la marca, lo que puede ser mas
     * fiable en caso de que el sitio tenga elementos superpuestos o problemas de clicabilidad con Selenium 
     */
    public void clickBrand(WebElementFacade brandCard) {
        try {
           
            scrollToElement(brandCard);
            waitABit(300);
            
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            js.executeScript("arguments[0].click();", brandCard);
            
            waitABit(2000);
            
        } catch (Exception e) {
            throw new RuntimeException("No se pudo hacer clic en la marca", e);
        }
    }

    // Metodo para obtener el nombre de una marca a partir de su tarjeta
    public String getBrandName(WebElementFacade brandCard) {
        try {
        
            String brandName = brandCard.getText().trim();
            
            /**
             * Medida de respaldo: intentar obtener el nombre de la marca desde un elemento hijo 
             * en caso de que el texto directo de la tarjeta esté vacío. Usada en los 
             * siguientes dos condicionales.
             */
            if (brandName.isEmpty()) {
                brandName = brandCard.findBy(By.tagName("h2")).getText().trim();
            }
            
            if (brandName.isEmpty()) {
                brandName = brandCard.findBy(By.tagName("h3")).getText().trim();
            }
            
            return brandName;
            
        } catch (Exception e) {
            return "Unknown Brand";
        }
    }

    // Metodo para desplazarse hasta un elemento para asegurar que esté visible en pantalla
    private void scrollToElement(WebElementFacade element) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    // Metodo alternativo a waitABit para pausar la ejecucion por un tiempo determinado
    public void pause(long milliseconds) {
        waitABit(milliseconds);
    }
}