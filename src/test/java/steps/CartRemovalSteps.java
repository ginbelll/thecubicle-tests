package steps;

import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.CartPage;
import pages.HomePage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Escenario de prueba: Agregar 5 productos al carrito, eliminar 3 de ellos y verificar que los 2 restantes hayan estado ahi desde el inicio.
 */
public class CartRemovalSteps {

    private static final Logger logger = LoggerFactory.getLogger(CartRemovalSteps.class);

    private static final String LOG_SEPARATOR = "========================================";
    private final HomePage homePage;
    private final CartPage cartPage;

    // Guardar informacion de los productos para verificar al final
    private List<String> allProductNames = new ArrayList<>();            // 5 productos agregados inicialmente
    private List<String> productsToRemove = new ArrayList<>();           // 3 productos a remover
    private List<String> expectedRemainingProducts = new ArrayList<>();  // 2 productos sobrantes

    // Constructor para inyectar el WebDriver desde los Steps
    public CartRemovalSteps(WebDriver driver) {
        this.homePage = new HomePage();
        this.cartPage = new CartPage();
        homePage.setDriver(driver);
        cartPage.setDriver(driver);
    }

    // Metodo para construir una lista de nombres de productos candidatos a agregar al carrito, asegurando que sean únicos y tengan el formato esperado.
    private List<String> buildCandidateProductNames(List<WebElementFacade> allProducts) {
        List<String> candidateProductNames = new ArrayList<>();
        Set<String> uniqueCandidateKeys = new HashSet<>();

        for (WebElementFacade product : allProducts) {
            try {
                String name = product.findElement(By.tagName("p")).getText().trim();
                String key = normalizeProductName(name);
                if (!name.isEmpty() && uniqueCandidateKeys.add(key)) {
                    candidateProductNames.add(name);
                }
            } catch (Exception ignored) {
                // Saltar productos que no tienen el formato esperado
            }
        }
        return candidateProductNames;
    }

    // Metodo para confirmar que un producto fue agregado al carrito
    private boolean confirmProductInCart(String productName) {
        try {
            cartPage.openCartPopup();
            return cartPage.isProductPresentInCart(productName);
        } catch (Exception popupError) {
            // En caso de que el popup del carrito no se abra o no se pueda verificar, hacer una verificación adicional en la página de carrito para confirmar si el producto realmente se agregó o si es un error del popup.
            logger.warn("Popup inestable para '{}'. Verificando en pagina de carrito...", productName);
            if (cartPage.isProductPresentInCartPage(productName)) {
                returnToHomePage(); // Regresar a la página de inicio para continuar con el flujo normal del test si se encontro el producto en la página de carrito
                return true;
            }
            return false;
        }
    }

    // Metodo para intentar agregar un producto al carrito con reintentos y confirmaciones
    private boolean tryAddProduct(String productName, int productNumber) {
        logger.info(LOG_SEPARATOR);
        logger.info("PRODUCTO #{}", productNumber);
        logger.info(LOG_SEPARATOR);
        logger.info("Nombre: {}", productName);

        for (int attempt = 1; attempt <= 3; attempt++) {
            logger.info("Agregando al carrito... (intento {}/3)", attempt);

            WebElementFacade productCard = homePage.findProductCardByName(productName);
            if (productCard == null) {
                logger.warn("No se encontro tarjeta visible para '{}'.", productName);
                return false;
            }

            try {
                homePage.addProductToCart(productCard);
                if (confirmProductInCart(productName)) {
                    return true;
                }
            } catch (Exception clickError) {
                logger.warn("Fallo click/agregado para '{}': {}", productName, clickError.getMessage());
            }

            logger.warn("No se confirmo agregado para '{}'. Reintentando...", productName);
            cartPage.closeCartPopup();
            cartPage.pause(700);
        }
        return false;
    }

    // Metodo para verificar que los productos agregados al carrito sean los correctos
    private void verifyAddedProducts() {
        cartPage.openCartPopup();
        List<String> actualProductsInCart = cartPage.getProductNamesInCart();
        List<String> verifiedProducts = new ArrayList<>();

        /** Comparar los productos que se intentaron agregar con los que realmente 
         * están en el carrito para confirmar que los 5 productos agregados inicialmente 
         * son los que se encuentran en el carrito, considerando posibles variaciones menores en los nombres.       
        */ 
        for (String expectedName : allProductNames) {
            for (String actualName : actualProductsInCart) {
                if (sameProductName(expectedName, actualName) && !verifiedProducts.contains(actualName)) {
                    verifiedProducts.add(actualName);
                    break;
                }
            }
        }

        if (verifiedProducts.size() < 5) {
            throw new IllegalStateException(
                "No hay 5 productos reales en carrito. Confirmados por click: "
                    + allProductNames
                    + " | Reales en carrito: "
                    + actualProductsInCart
            );
        }

        // Verificar que los productos confirmados sean únicos para evitar falsos positivos por duplicados
        Set<String> uniqueVerifiedKeys = new HashSet<>();
        for (String verifiedName : verifiedProducts) {
            uniqueVerifiedKeys.add(normalizeProductName(verifiedName));
        }

        if (uniqueVerifiedKeys.size() < 5) {
            throw new IllegalStateException(
                "Se detectaron repeticiones. Se requieren 5 productos diferentes. Reales en carrito: "
                    + actualProductsInCart
            );
        }

        allProductNames = verifiedProducts;
    }

    //este mismo link está hardcodeado en la clase homepage

    // Metodo para abrir la página de inicio de TheCubicle
    public void openHomePage() {
        logger.info("\n" + LOG_SEPARATOR);
        logger.info("ABRIENDO PAGINA DE INICIO");
        logger.info(LOG_SEPARATOR);
        homePage.getDriver().get("https://www.thecubicle.com/en-global");
        logger.info("Pagina de inicio cargada\n");
    }

    // Metodo para agregar 5 productos aleatorios al carrito desde la página de inicio
    public void addFiveRandomProducts() {
        allProductNames.clear();

        logger.info(LOG_SEPARATOR);
        logger.info("AGREGANDO 5 PRODUCTOS AL CARRITO");
        logger.info(LOG_SEPARATOR + "\n");

        List<WebElementFacade> allProducts = homePage.getAllVisibleProducts();
        logger.info("Encontrados {} productos en total\n", allProducts.size());

        if (allProducts.size() < 5) {
            throw new IllegalStateException("No hay suficientes productos. Se encontraron: " + allProducts.size());
        }

        List<String> candidateProductNames = buildCandidateProductNames(allProducts);
        Collections.shuffle(candidateProductNames);

        int confirmedAdds = 0;

        // Intentar agregar productos de la lista de candidatos hasta confirmar que 5 productos reales están en el carrito, o agotar la lista de candidatos.
        for (String productName : candidateProductNames) {
            if (confirmedAdds == 5) {
                break;
            }

            if (tryAddProduct(productName, confirmedAdds + 1)) {
                allProductNames.add(productName);
                confirmedAdds++;
                logger.info("Agregado y confirmado: {}", productName);

                if (confirmedAdds < 5) {
                    cartPage.closeCartPopup();
                    logger.info("Cerrando popup...\n");
                } else {
                    logger.info("Manteniendo popup abierto (ultimo producto)\n");
                }
            } else {
                List<String> snapshot;
                try {
                    snapshot = cartPage.getProductNamesFromCartPage();
                    returnToHomePage();
                } catch (Exception snapshotError) {
                    snapshot = new ArrayList<>();
                }

                throw new IllegalStateException(
                    "[FAIL_FAST_CART_ADD_BUG] No se pudo confirmar agregado real para '"
                        + productName
                        + "' despues de 3 intentos. Carrito actual: "
                        + snapshot
                );
            }
        }

        if (allProductNames.size() < 5) {
            throw new IllegalStateException(
                "No se pudieron confirmar 5 productos en carrito. Confirmados: " + allProductNames.size()
            );
        }

        verifyAddedProducts();

        logger.info(LOG_SEPARATOR);
        logger.info("5 PRODUCTOS AGREGADOS AL CARRITO:");
        logger.info(LOG_SEPARATOR);
        for (int i = 0; i < allProductNames.size(); i++) {
            logger.info("{}. {}", (i + 1), allProductNames.get(i));
        }
        logger.info(LOG_SEPARATOR + "\n");

        cartPage.closeCartPopup();
    }

    // Metodo para seleccionar aleatoriamente 3 productos de los 5 agregados inicialmente 
    public void selectThreeProductsToRemove() {

        productsToRemove.clear();
        expectedRemainingProducts.clear();

        logger.info(LOG_SEPARATOR);
        logger.info("SELECCIONANDO 3 PRODUCTOS PARA ELIMINAR");
        logger.info(LOG_SEPARATOR + "\n");

        List<String> shuffledNames = new ArrayList<>(allProductNames);
        Collections.shuffle(shuffledNames); // Mezclar la lista de productos para seleccionar aleatoriamente

        productsToRemove = shuffledNames.subList(0, 3).stream().collect(Collectors.toList()); // Tomar los primeros 3 productos de la lista mezclada para eliminar
        expectedRemainingProducts = shuffledNames.subList(3, 5).stream().collect(Collectors.toList()); // Los últimos 2 productos de la lista mezclada serán los que se espera que permanezcan en el carrito 
 
        logger.info("Productos a ELIMINAR:");
        for (String name : productsToRemove) {
            logger.info(" {}", name);
        }

        logger.info("\nProductos que deben PERMANECER:");
        for (String name : expectedRemainingProducts) {
            logger.info(" {}", name);
        }
        logger.info("\n");
    }

    // Metodo para eliminar los 3 productos seleccionados del carrito
    public void removeSelectedProducts() {

        logger.info(LOG_SEPARATOR);
        logger.info("ELIMINANDO 3 PRODUCTOS DEL CARRITO");
        logger.info(LOG_SEPARATOR + "\n");

        cartPage.openCartPopup();

        // Iterar sobre los productos seleccionados para eliminar
        for (String productName : productsToRemove) {
            logger.info("Eliminando: {}", productName);

            WebElementFacade productRow = cartPage.findProductInCart(productName);
            cartPage.removeProduct(productRow);

            logger.info("Eliminado: {}\n", productName);
            cartPage.pause(2000);
        }

        logger.info(LOG_SEPARATOR);
        logger.info("3 PRODUCTOS ELIMINADOS");
        logger.info(LOG_SEPARATOR + "\n");
    }

    // Metodo para verificar que solo quedan los 2 productos esperados en el carrito
    public void verifyRemainingProducts() {

        logger.info(LOG_SEPARATOR);
        logger.info("VERIFICANDO PRODUCTOS RESTANTES");
        logger.info(LOG_SEPARATOR + "\n");

        List<String> actualRemainingProducts = cartPage.getProductNamesInCart();

        logger.info("Productos esperados en carrito:");
        for (String name : expectedRemainingProducts) {
            logger.info("  {}", name);
        }

        logger.info("\nProductos realmente en carrito:");
        for (String name : actualRemainingProducts) {
            logger.info("  {}", name);
        }

        logger.info("\n" + LOG_SEPARATOR);

        if (actualRemainingProducts.size() != 2) {
            throw new AssertionError(
                "ERROR: Se esperaban 2 productos en el carrito, pero hay: " + actualRemainingProducts.size()
            );
        }

        for (String expectedProduct : expectedRemainingProducts) {
            if (!actualRemainingProducts.contains(expectedProduct)) {
                throw new AssertionError("ERROR: Producto esperado no encontrado en carrito: " + expectedProduct);
            }
        }

        logger.info(LOG_SEPARATOR);
        logger.info("TEST COMPLETADO CON EXITO");
        logger.info(LOG_SEPARATOR);
        logger.info("Los 2 productos restantes coinciden con la seleccion original");
        logger.info(LOG_SEPARATOR + "\n");
    }

    // Metodo para comparar nombres de productos de manera flexible, considerando posibles variaciones menores en el formato o texto.
    private boolean sameProductName(String expected, String actual) {
        String expectedNormalized = normalizeProductName(expected);
        String actualNormalized = normalizeProductName(actual);
        return expectedNormalized.equals(actualNormalized)
            || expectedNormalized.contains(actualNormalized)
            || actualNormalized.contains(expectedNormalized);
    }

    // Metodo para normalizar el nombre del producto
    private String normalizeProductName(String name) {
        return name.toLowerCase().trim();
    }

    // Metodo para regresar a la página de inicio después de verificar el popup del carrito
    private void returnToHomePage() {
        homePage.getDriver().get("https://www.thecubicle.com/en-global");
        homePage.getAllVisibleProducts();
    }
}