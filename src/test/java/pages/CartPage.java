package pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class CartPage extends PageObject {
    
    // Definir los elementos de la pagina
    @FindBy(css = ".cart-modal-form")
    private WebElementFacade cartPopup; // Ventana del carrito

    @FindBy(css = ".modal__backdrop")  
    private WebElementFacade overlay; // Fondo para cerrar el carrito
    
    @FindBy(css = ".cart-subtotal--price small")
    private WebElementFacade totalPrice; // Precio total mostrado en el carrito

    @FindBy(css = "a[href='/en-global/cart']")
    private WebElementFacade cartIcon; // Icono del carrito para abrirlo

    @FindBy(xpath = "//form[contains(@class,'cart-modal-form')]//a[contains(@href,'/cart') and contains(.,'Show Entire Cart')]")
    private WebElementFacade showEntireCartLink; // Link para mostrar el carrito completo en otra pestaña

    // Esperar a que el carrito aparezca
    public void waitForCartPopup() {
        waitABit(500);
        try {
            cartPopup.waitUntilVisible().withTimeoutOf(Duration.ofSeconds(5));
        } catch (Exception e) { // Finalizar si el carrito no aparece
            throw new RuntimeException("El carrito no se mostró después de agregar productos", e);
        }
    }

    // Cerrar el carrito haciendo clic en el fondo
    public void closeCartPopup() {
      try {
          waitForCartPopup();
          
          JavascriptExecutor js = (JavascriptExecutor) getDriver();
          js.executeScript("arguments[0].click();", overlay);
          
          // Esperar a que desaparezca totalmente la ventana del carrito         
          waitABit(1000);
          overlay.waitUntilNotVisible().withTimeoutOf(Duration.ofSeconds(5));
          
      } catch (Exception e) {
          // Si aun no ha desaparecido, esperar aun mas
          waitABit(2000);
      }
    }

    // Obtener el total mostrado en el carrito, con reintentos para manejar retrasos en la actualización del DOM
    public String getCartTotal() {
        waitForCartPopup();

        Exception lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) { // Se hacen 3 intentos para obtener el precio total actualizado
            try {
                totalPrice.waitUntilVisible().withTimeoutOf(Duration.ofSeconds(5));
                String text = totalPrice.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return text;
                }
            } catch (Exception e) {
                lastError = e;
            }

            waitABit(400);
        }

        throw new RuntimeException("No se puede leer el total del carrito", lastError);
    }

    // Abrir el carrito haciendo clic en el icono y esperar a que se muestre completamente
    public void openCartPopup() {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].click();", cartIcon);
        
        // Esperar a que aparezca totalmente la ventana del carrito junto con su contenido
        waitABit(1000);
        waitForCartPopup();
        showEntireCartIfAvailable();
        waitForCartContentToLoad();
    }

    // Si el link para mostrar el carrito completo es visible, hacer clic en él para asegurar que todos los productos han sido agregados
    private void showEntireCartIfAvailable() {
        try {
            if (showEntireCartLink.isCurrentlyVisible()) {
                JavascriptExecutor js = (JavascriptExecutor) getDriver();
                js.executeScript("arguments[0].click();", showEntireCartLink);
                waitABit(700);
            }
        } catch (Exception ignored) {
            // Ignorar si no es posible ejecutar el metodo
        }
    }

    // Metodo para esperar que cargue el contenido dentro del carrito, verificando que al menos un producto sea visible antes de continuar
    private void waitForCartContentToLoad() {
        for (int attempt = 0; attempt < 20; attempt++) {
            if (!findAll(".cart-row").isEmpty()) {
                return;
            }
            waitABit(250);
        }
    }

    // Buscar un producto específico dentro del carrito, con múltiples intentos para manejar retrasos en la actualización del DOM
    public WebElementFacade findProductInCart(String productName) {
        waitForCartPopup();

        List<String> lastSeenProducts = new ArrayList<>(); // Lista para almacenar los nombres de productos vistos en cada intento, para diagnosticar problemas de actualización del carrito

        // Hacer varios intentos para encontrar el producto, refrescando la lista de productos visibles en cada intento para manejar retrasos en la actualización del DOM después de agregar productos al carrito
        for (int attempt = 1; attempt <= 4; attempt++) {
            showEntireCartIfAvailable();
            waitForCartContentToLoad();

            List<WebElementFacade> cartItems = findAll(".cart-row");
            lastSeenProducts.clear();

            // Iterar sobre los productos visibles en el carrito para encontrar el que coincida con el nombre esperado, almacenando los nombres de los productos vistos en cada intento para diagnosticar problemas de actualización del carrito
            for (WebElementFacade item : cartItems) {
                try {
                    String itemName = item.findBy(By.cssSelector(".large--three-quarters a")).getText().trim();
                    lastSeenProducts.add(itemName);
                    if (namesMatch(itemName, productName)) {
                        return item;
                    }
                } catch (Exception e) {
                    // Continuar buscando si no se encuentra el nombre
                }
            }

            waitABit(500);
        }

        throw new RuntimeException(
            "Producto no encontrado en el carrito: " + productName + ". Productos visibles: " + lastSeenProducts
        );
    }

    // Verificar si un producto específico está presente en el carrito, con múltiples intentos para manejar retrasos en la actualización del DOM
    public boolean isProductPresentInCart(String productName) {
        waitForCartPopup();

        // Hacer varios intentos para encontrar el producto, refrescando la lista de productos visibles en cada intento para manejar retrasos en la actualización del DOM después de agregar productos al carrito
        for (int attempt = 1; attempt <= 8; attempt++) {
            showEntireCartIfAvailable();
            waitForCartContentToLoad();

            // Iterar sobre los productos visibles en el carrito para verificar si alguno coincide con el nombre esperado, refrescando la lista de productos visibles en cada intento para manejar retrasos en la actualización del DOM después de agregar productos al carrito
            List<WebElementFacade> cartItems = findAll(".cart-row");
            for (WebElementFacade item : cartItems) {
                try {
                    String itemName = item.findBy(By.cssSelector(".large--three-quarters a")).getText().trim();
                    if (namesMatch(itemName, productName)) {
                        return true;
                    }
                } catch (Exception ignored) {
                    // Continuar buscando si no se encuentra el nombre
                }
            }
            waitABit(350);
        }

        return false;
    }

    // Método para comparar nombres de productos, considerando posibles variaciones en mayúsculas, espacios y orden de palabras
    private boolean namesMatch(String itemName, String productName) {
        String normalizedItem = itemName.toLowerCase();
        String normalizedProduct = productName.toLowerCase();
        return normalizedItem.equals(normalizedProduct)
            || normalizedProduct.contains(normalizedItem)
            || normalizedItem.contains(normalizedProduct);
    }

    // Método para obtener los nombres de los productos actualmente visibles en la página del carrito
    public List<String> getProductNamesFromCartPage() {
        getDriver().get("https://www.thecubicle.com/en-global/cart"); // Pagina del carrito
        waitABit(1200);

        List<String> productNames = new ArrayList<>(); 
        List<WebElementFacade> cartItems = findAll(".cart-row"); // Lista de productos visibles en el carrito

        // Iterar sobre los productos visibles en el carrito para extraer sus nombres
        for (WebElementFacade item : cartItems) {
            try {
                String itemName = item.findBy(By.cssSelector(".large--three-quarters a")).getText();
                if (itemName != null && !itemName.trim().isEmpty()) {
                    productNames.add(itemName.trim());
                }
            } catch (Exception ignored) {
                // Continuar si no se puede extraer el nombre de un producto específico
            }
        }

        return productNames;
    }

    // Verificar si un producto específico está presente en la página del carrito
    public boolean isProductPresentInCartPage(String productName) {
        List<String> productNames = getProductNamesFromCartPage();
        for (String name : productNames) {
            if (namesMatch(name, productName)) {
                return true;
            }
        }
        return false;
    }

    // Método para eliminar un producto específico del carrito, haciendo clic en el botón de eliminar asociado a ese producto
    public void removeProduct(WebElementFacade productRow) {
        try {
            WebElementFacade removeButton = productRow.findBy(By.cssSelector(".remove"));
            
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            js.executeScript("arguments[0].click();", removeButton);

            waitABit(1000); // Esperar a que el producto se elimine del DOM
        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el producto del carrito: " + e.getMessage(), e);
        }
    }

    // Método para obtener los nombres de los productos actualmente visibles en el carrito
    public List<String> getProductNamesInCart() {
        waitForCartPopup();
        showEntireCartIfAvailable();
        waitForCartContentToLoad();

        List<String> productNames = new ArrayList<>();

        List<WebElementFacade> cartItems = findAll(".cart-row");

        // Iterar sobre los productos visibles en el carrito para extraer sus nombres
        for (WebElementFacade item : cartItems) {
            try {
                String itemName = item.findBy(By.cssSelector(".large--three-quarters a")).getText();
                productNames.add(itemName.trim());
            } catch (Exception e) {
                // Continuar si no se puede extraer el nombre
                continue;
            }
        }
        return productNames;
    }

    // Método para obtener la cantidad de productos actualmente visibles en el carrito
    public int getCartItemCount() {
        waitForCartPopup();
        showEntireCartIfAvailable();
        waitForCartContentToLoad();

        return findAll(".cart-row").size();
    }

    // Método para obtener la cantidad de productos en el carrito consultando directamente la API
    public int getCartItemCountFromApi() {
        try {
            getDriver().manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            Object result = js.executeAsyncScript(
                "const done = arguments[arguments.length - 1];" +
                "fetch('/en-global/cart.js', { credentials: 'same-origin', cache: 'no-store' })" +
                ".then(r => r.ok ? r.json() : Promise.reject('HTTP ' + r.status))" +
                ".then(data => done(Number(data.item_count)))" +
                ".catch(err => done('ERROR:' + err));"
            ); // Ejecutar un script asíncrono para consultar la API del carrito y obtener la cantidad de productos, manejando posibles errores de red o respuestas no exitosas

            if (result instanceof Number) {
                return ((Number) result).intValue();
            }

            if (result == null) {
                throw new RuntimeException("Respuesta vacia al consultar /cart.js");
            }

            String value = result.toString().trim();
            if (value.startsWith("ERROR:")) {
                throw new RuntimeException("Error consultando /cart.js: " + value);
            }

            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer la cantidad real del carrito", e);
        }
    }

    // Método para obtener la cantidad de productos en el carrito consultando directamente la interfaz de usuario
    public int getCartItemCountFromUi() {
        openCartPopup();
        return getCartItemCount();
    }

    // Método para esperar hasta que la cantidad de productos en el carrito sea al menos la cantidad esperada, consultando directamente la interfaz de usuario, con un tiempo máximo de espera para evitar esperas indefinidas
    public boolean waitForCartItemCountAtLeast(int expectedCount, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);

        // Mientras no se alcance el tiempo máximo de espera, consultar la cantidad de productos en el carrito a través de la interfaz de usuario y verificar si es al menos la cantidad esperada
        while (System.currentTimeMillis() < deadline) {
            try {
                int currentCount = getCartItemCountFromUi();
                if (currentCount >= expectedCount) {
                    return true;
                }
            } catch (Exception ignored) {
                // Ignorar errores al obtener la cantidad del carrito
            }

            waitABit(400);
        }

        return false;
    }

    // Metodo alternativo a waitABit para pausar la ejecución por un tiempo específico
    public void pause(long milliseconds) {
        waitABit(milliseconds);
    }
}
