package steps;

import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.HomePage;
import pages.CartPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aqui definimos los pasos para el test de agregar productos al carrito y verificar el total.
 * 1. Seleccionar 3 productos aleatorios de la página de inicio
 * 2. Agregarlos al carrito de compras
 * 3. Verificar que el total del carrito coincida con la suma de los precios individuales de los productos seleccionados
 */
public class ProductSteps {

    // Definir logger para hacer seguimiento de los pasos y resultados
    private static final Logger logger = LoggerFactory.getLogger(ProductSteps.class);

    // Objetos de la pagina
    private HomePage homePage;
    private CartPage cartPage;

    // Listas para almacenar los datos de los productos seleccionados
    private List<Double> selectedPrices = new ArrayList<>();
    private List<String> selectedProductNames = new ArrayList<>();

    /**
     * Constructor - Inicializa los objetos de página y asigna el WebDriver gestionado por Serenity
     * @param driver El webdriver gestionado por Serenity
     */
    public ProductSteps(WebDriver driver) {
        this.homePage = new HomePage();
        this.cartPage = new CartPage();
        homePage.setDriver(driver);
        cartPage.setDriver(driver);
    }

    // PASO 2: Seleccionar 3 productos aleatorios y agregarlos al carrito
    public void selectThreeRandomProductsAndAddToCart() {
        // Asegurarse de que siempre iniciemos con una lista vacia
        selectedPrices.clear();
        selectedProductNames.clear();

        logger.info("========================================");
        logger.info("SELECCIONANDO Y AGREGANDO PRODUCTOS AL CARRITO");
        logger.info("========================================\n");

        // Conseguir todos los productos visibles en la página de inicio
        List<WebElementFacade> allProducts = homePage.getAllVisibleProducts();
        logger.info("Encontrados {} productos en total\n", allProducts.size());

        // Verificar que hay suficientes productos para seleccionar
        if (allProducts.size() < 3) {
            throw new RuntimeException("No hay suficientes productos en el sitio: " + allProducts.size());
        }

        // Crear una copia de la lista para mezclar y seleccionar aleatoriamente sin modificar la original
        List<WebElementFacade> copyOfProducts = new ArrayList<>(allProducts);
        Collections.shuffle(copyOfProducts);

        // Variables para controlar el proceso de selección
        int idx = 0; // Índice para iterar sobre los productos mezclados
        int attempts = 0; // Contador de intentos para evitar loops infinitos
        double previousCartSubtotal = 0.0; // Para calcular el precio del producto a partir del cambio en el subtotal del carrito

        // Intentar con diferentes productos hasta que se hayan elegido tres o no hayan mas disponibles
        while (selectedPrices.size() < 3 && idx < copyOfProducts.size() && attempts < copyOfProducts.size()) {
            WebElementFacade product = copyOfProducts.get(idx);
            attempts++;

            logger.info("========================================");
            logger.info("PRODUCTO #{}", (selectedPrices.size() + 1));
            logger.info("========================================");

            // Extraer el nombre del producto para mejor seguimiento en los logs
            String productName = "(Unknown)";
            try {
                productName = product.findElement(By.tagName("p")).getText();
                logger.info("Nombre: {}", productName);
            } catch (Exception e) {
                logger.info("Nombre: (No se pudo extraer el nombre)");
            }

            // Agregar el producto al carrito
            logger.info("Agregando producto al carrito...");
            homePage.addProductToCart(product);

            /** Se calcula el precio de cada producto a partir del cambio en el subtotal del carrito después de agregarlo
            * Para evitar lidiar con diferentes formatos de precio
            * Precio del producto = Subtotal del carrito después de agregar - Subtotal del carrito antes de agregar    
            */
            logger.info("Leyendo subtotal del carrito...");
            String cartSubtotalText = cartPage.getCartTotal();
            logger.info("Texto del subtotal: '{}'", cartSubtotalText);
            
            double currentCartSubtotal = parsePriceToDouble(cartSubtotalText);
            logger.info("Subtotal parseado: ${}", String.format("%.2f", currentCartSubtotal));
            
            // Calcular el precio del producto a partir del cambio en el subtotal del carrito (Delta)
            double priceDelta = currentCartSubtotal - previousCartSubtotal;
            double price = Math.round(priceDelta * 100.0) / 100.0;
            
            logger.info("Precio subtotal anterior: ${}", String.format("%.2f", previousCartSubtotal));

            // Verificar si el producto se agregó correctamente al carrito, si delta es muy pequeño o negativo, el producto no se agregó correctamente
            if (priceDelta <= 0.004) {
                logger.info("No se pudo elegir este producto, intentando con otro...");
                cartPage.closeCartPopup();
                idx++;
                continue;
            }

            // Guardar datos del producto agregado correctamente
            selectedPrices.add(price);
            selectedProductNames.add(productName);
            previousCartSubtotal = currentCartSubtotal;

            logger.info("Producto agregado con exito");
            logger.info("Precio del producto: ${}", String.format("%.2f", price));
            logger.info("Total actual: ${}", String.format("%.2f", previousCartSubtotal));

            // Cerrar la ventana del carrito, excepto en el tercer producto para obtener el subtotal
            if (selectedPrices.size() < 3) {
                logger.info("Cerrando ventana del carrito..\n");
                cartPage.closeCartPopup();
            } else {
                logger.info("Producto final seleccionado\n");
            }

            idx++;
        }

        // Verificar que si se hayan agregado 3 productos
        if (selectedPrices.size() < 3) {
            throw new RuntimeException("No se pudieron agregar 3 productos al carrito, se agregaron: " + selectedPrices.size());
        }

        // Mostrar una lista de todos los productos seleccionados
        logger.info("========================================");
        logger.info("LISTA DE PRODUCTOS AGREGADOS");
        logger.info("========================================");
        for (int i = 0; i < selectedPrices.size(); i++) {
            logger.info("{}. {} → ${}", (i + 1), selectedProductNames.get(i), 
                             String.format("%.2f", selectedPrices.get(i)));
        }
        logger.info("========================================\n");
    }

    // PASO 3: Verificar que el total del carrito coincida con la suma total
    public void verifyCartTotal() {
        logger.info("========================================");
        logger.info("VERIFICANDO TOTAL DEL CARRITO");
        logger.info("========================================\n");

        // Sacar subtotal del carrito
        logger.info("Leyendo subtotal del carrito..");
        String cartTotalText = cartPage.getCartTotal();
        logger.info("Subtotal en carrito: '{}'", cartTotalText);

        // Convertir texto a numero para poder compararlo
        double cartTotal = parsePriceToDouble(cartTotalText);

        // Calcular suma de todos los productos seleccionados aleatoriamente
        double expectedTotal = selectedPrices.stream().mapToDouble(Double::doubleValue).sum();

        logger.info("\n========================================");
        logger.info("RESUMEN DE PRECIOS");
        logger.info("========================================");
        for (int i = 0; i < selectedPrices.size(); i++) {
            logger.info("Producto {}: ${}", (i + 1), String.format("%.2f", selectedPrices.get(i)));
        }
        logger.info("========================================");
        logger.info("Suma de precios individuales: ${}", String.format("%.2f", expectedTotal));
        logger.info("Total en carrito:  ${}", String.format("%.2f", cartTotal));
        logger.info("Diferencia:               ${}", String.format("%.2f", Math.abs(expectedTotal - cartTotal)));
        logger.info("========================================\n");

        // Comparar subtotales
        double tolerance = 0.02; // Tolerancia de 2 centavos para evitar fallos por redondeo o formatos de precio
        if (Math.abs(expectedTotal - cartTotal) <= tolerance) {
            logger.info("========================================");
            logger.info("TEST COMPLETADO CON EXITO");
            logger.info("========================================");
            logger.info("Los precios coinciden");
            logger.info("Precio calculado: ${}", String.format("%.2f", expectedTotal));
            logger.info("Precio real:   ${}", String.format("%.2f", cartTotal));
            logger.info("========================================\n");
        } else {
            logger.info("========================================");
            logger.info("TEST FALLADO");
            logger.info("========================================");
            logger.info("Los precios no coinciden");
            logger.info("Precio calculado: ${}", String.format("%.2f", expectedTotal));
            logger.info("Precio real:   ${}", String.format("%.2f", cartTotal));
            logger.info("Diferencia: ${}", String.format("%.2f", Math.abs(expectedTotal - cartTotal)));
            logger.info("========================================\n");
            
            throw new AssertionError(String.format(
                "ERROR: Precio no coincidente! Se calculó: $%.2f, pero el carrito muestra: $%.2f (Difference: $%.2f)",
                expectedTotal, cartTotal, Math.abs(expectedTotal - cartTotal)));
        }
    }

    // Metodo auxiliar para convertir el texto del precio a un valor double, manejando diferentes formatos y símbolos
    private double parsePriceToDouble(String priceText) {
        // Preferir encontrar un número decimal con dos dígitos después del punto usando regex, para evitar problemas con diferentes formatos de precio
        java.util.regex.Pattern decimalPattern = java.util.regex.Pattern.compile("(\\d+\\.\\d{2})");
        java.util.regex.Matcher m = decimalPattern.matcher(priceText.replaceAll(",", ""));
        String found = null;
        while (m.find()) {
            found = m.group(1); 
        }
        if (found != null) {
            return Double.parseDouble(found);
        }

        // Si no se encuentra un número decimal, limpiar el texto para extraer solo dígitos y puntos, e interpretar el resultado
        String cleaned = priceText.replaceAll("[^0-9.]", "");

        if (cleaned.isEmpty()) {
            return 0.0;
        }

        // Si no hay un punto decimal, interpretar los últimos dos dígitos como centavos
        if (!cleaned.contains(".")) {
            if (cleaned.length() <= 2) {
                // Para numeros de 1 o 2 digitos, tratar como centavos: "99" -> "0.99"
                cleaned = "0." + String.format("%02d", Integer.parseInt(cleaned));
            } else {
                // Para numeros con mas de 3 digitos, los ultimos 2 son centavos: "1234" -> "12.34"
                int len = cleaned.length();
                cleaned = cleaned.substring(0, len - 2) + "." + cleaned.substring(len - 2);
            }
        }

        // Convertir string a double
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw e;
        }
    }
}