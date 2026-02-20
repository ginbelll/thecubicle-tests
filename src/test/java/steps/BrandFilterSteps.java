package steps;

import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.WebDriver;
import pages.PopularBrandsPage;
import pages.BrandSortPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

/**
 * Escenario de test: Seleccionar una marca aleatoria del dropdown de marcas
 * y verificar que el ordenamiento por orden alfabetico A-Z se aplica correctamente.
 */
public class BrandFilterSteps {

    private static final Logger logger = LoggerFactory.getLogger(BrandFilterSteps.class); // Logger para hacer seguimiento a la ejecucion

    private PopularBrandsPage popularBrandsPage;
    private BrandSortPage brandSortPage;
    private String selectedBrand; // Variable para almacenar la marca seleccionada y usarla en otros pasos

    public BrandFilterSteps(WebDriver driver) {
        this.popularBrandsPage = new PopularBrandsPage();
        this.brandSortPage = new BrandSortPage();
        popularBrandsPage.setDriver(driver);
        brandSortPage.setDriver(driver);
    }

    // Metodo para abrir la pagina de marcas populares
    public void openPopularBrandsPage() {
        logger.info("\n========================================");
        logger.info("ABRIENDO PAGINA MARCAS POPULARES");
        logger.info("========================================");
        popularBrandsPage.getDriver().get("https://www.thecubicle.com/en-global/pages/collections/top-brands");
        logger.info("Pagina de marcas populares cargada\n");
    }

    public void selectRandomBrand() {
        logger.info("========================================");
        logger.info("SELECCIONANDO MARCA ALEATORIA");
        logger.info("========================================\n");

        // Obtener todas las marcas disponibles en la página
        List<WebElementFacade> allBrands = popularBrandsPage.getAllBrands();
        
        logger.info("Marcas disponibles: {}", allBrands.size());

        // Validar que hay marcas
        if (allBrands.isEmpty()) {
            throw new RuntimeException("No se encontraron marcas en la página");
        }

        // Advertir si no hay exactamente 6 marcas
        if (allBrands.size() != 6) {
            logger.warn("Se esperaban 6 marcas, pero se encontraron: {}", allBrands.size());
        }

        // Seleccionar una marca aleatoria
        Random random = new Random();
        int randomIndex = random.nextInt(allBrands.size());
        WebElementFacade randomBrand = allBrands.get(randomIndex);

        // Obtener el nombre de la marca antes de hacer clic
        selectedBrand = popularBrandsPage.getBrandName(randomBrand);
        
        logger.info("Marca seleccionada: {}", selectedBrand);
        logger.info("Haciendo clic en la marca...");

        // Hacer clic en la marca para navegar a su página
        popularBrandsPage.clickBrand(randomBrand);

        logger.info("Marca '{}' seleccionada", selectedBrand);
        logger.info("Navegando a página de marca...\n");
    }

    // Metodo para ordenar los productos de la marca seleccionada alfabeticamente de la A a la Z
    public void sortProductsAlphabetically() {
        logger.info("========================================");
        logger.info("ORDENANDO PRODUCTOS A-Z");
        logger.info("========================================\n");

        brandSortPage.waitForProductsToLoad();
        brandSortPage.pause(1000);

        logger.info("Abriendo dropdown de ordenamiento...");
        brandSortPage.clickSortDropdown();

        logger.info("Seleccionando orden alfabético (A-Z)...");
        brandSortPage.selectSortOptionAZ();

        logger.info("Esperando a que se reordenen los productos...");
        brandSortPage.pause(3000);

        logger.info("Productos ordenados alfabéticamente\n");
    }

    // Verificar que los productos se ordenaron correctamente de la A a la Z
    public void verifyAlphabeticalOrder() {
        logger.info("========================================");
        logger.info("VERIFICANDO ORDEN ALFABÉTICO");
        logger.info("========================================\n");

        List<String> productNames = brandSortPage.getAllProductNamesOnPage();

        logger.info("Productos encontrados en la página: {}", productNames.size());

        if (productNames.isEmpty()) {
            throw new RuntimeException("No se encontraron productos en la página");
        }

        logger.info("\nNombres de productos (en orden mostrado):");
        for (int i = 0; i < productNames.size(); i++) {
            logger.info("  {}. {}", (i + 1), productNames.get(i));
        }

        logger.info("\n--- VERIFICANDO ORDEN ---");
        boolean isCorrectlyOrdered = true;
        List<String> outOfOrderProducts = new ArrayList<>();

        for (int i = 0; i < productNames.size() - 1; i++) {
            String current = productNames.get(i).toLowerCase();
            String next = productNames.get(i + 1).toLowerCase();

            // Comparar los nombres de los productos para verificar el orden alfabético
            if (current.compareTo(next) > 0) {
                // Si el producto actual viene después del siguiente alfabeticamente, el orden es incorrecto
                isCorrectlyOrdered = false;
                String error = String.format("'%s' viene después de '%s'", 
                                           productNames.get(i), productNames.get(i + 1));
                outOfOrderProducts.add(error);
                logger.error("Orden incorrecto: {}", error);
            } else {
                logger.info("'{}' -> '{}'", productNames.get(i), productNames.get(i + 1));
            }
        }

        logger.info("\n========================================");

        if (isCorrectlyOrdered) {
            logger.info("TEST COMPLETADO CON ÉXITO");
            logger.info("========================================");
            logger.info("Todos los {} productos están ordenados alfabéticamente", productNames.size());
            logger.info("Marca: {}", selectedBrand);
            logger.info("========================================\n");
        } else {
            logger.error("TEST FALLADO");
            logger.error("========================================");
            logger.error("Productos NO están en orden alfabético");
            logger.error("Errores encontrados: {}", outOfOrderProducts.size());
            for (String error : outOfOrderProducts) {
                logger.error("  - {}", error);
            }
            logger.error("========================================\n");

            throw new AssertionError(
                String.format("Los productos NO están ordenados alfabéticamente. " +
                            "Se encontraron %d errores de ordenamiento.", 
                            outOfOrderProducts.size())
            );
        }
    }

    // Metodo para obtener la marca seleccionada
    public String getSelectedBrand() {
        return selectedBrand;
    }
}
