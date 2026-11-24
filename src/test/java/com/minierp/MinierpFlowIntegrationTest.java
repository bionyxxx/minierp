package com.minierp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.minierp.common.PaginationDefaults;
import com.minierp.common.ProblemTypes;
import com.minierp.docs.ProblemCatalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end flow against a real PostgreSQL (Testcontainers): authentication,
 * role restrictions, approval threshold and the purchase/sales stock flows.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class MinierpFlowIntegrationTest {

	private static final AtomicLong SEQ = new AtomicLong();

	@Autowired
	TestRestTemplate rest;

	@Autowired
	ObjectMapper mapper;

	// ---------- helpers ----------

	private String login(String username, String password) {
		ResponseEntity<String> response = rest.postForEntity("/api/auth/login",
				Map.of("username", username, "password", password), String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "login " + username);
		return read(response.getBody()).get("token").asString();
	}

	private HttpHeaders auth(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);
		return headers;
	}

	private JsonNode read(String body) {
		return mapper.readTree(body);
	}

	private ResponseEntity<String> post(String token, String url, Object body) {
		return rest.exchange(url, HttpMethod.POST, new HttpEntity<>(body, auth(token)), String.class);
	}

	private ResponseEntity<String> get(String token, String url) {
		return rest.exchange(url, HttpMethod.GET, new HttpEntity<>(auth(token)), String.class);
	}

	private long createItem(String token) {
		long n = SEQ.incrementAndGet();
		Map<String, Object> body = Map.of(
				"sku", "SKU-" + n,
				"name", "Item " + n,
				"uom", "PCS",
				"minStock", "0",
				"purchasePrice", "1000",
				"sellingPrice", "1500");
		ResponseEntity<String> response = post(token, "/api/items", body);
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create item");
		return read(response.getBody()).get("id").asLong();
	}

	private long createSupplier(String token) {
		long n = SEQ.incrementAndGet();
		ResponseEntity<String> response = post(token, "/api/suppliers",
				Map.of("code", "SUP-" + n, "name", "Supplier " + n));
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create supplier");
		return read(response.getBody()).get("id").asLong();
	}

	private long createCustomer(String token) {
		long n = SEQ.incrementAndGet();
		ResponseEntity<String> response = post(token, "/api/customers",
				Map.of("code", "CUS-" + n, "name", "Customer " + n));
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create customer");
		return read(response.getBody()).get("id").asLong();
	}

	private long createWarehouse(String token) {
		long n = SEQ.incrementAndGet();
		ResponseEntity<String> response = post(token, "/api/warehouses",
				Map.of("code", "WH-" + n, "name", "Gudang " + n, "location", "Bandung"));
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create warehouse");
		return read(response.getBody()).get("id").asLong();
	}

	private Map<String, Object> line(long itemId, String qty, String unitPrice) {
		Map<String, Object> line = new HashMap<>();
		line.put("itemId", itemId);
		line.put("qty", qty);
		line.put("unitPrice", unitPrice);
		return line;
	}

	private long createPo(String token, long supplierId, List<Map<String, Object>> lines) {
		Map<String, Object> body = Map.of(
				"supplierId", supplierId,
				"orderDate", "2026-11-23",
				"notes", "integration test",
				"lines", lines);
		ResponseEntity<String> response = post(token, "/api/purchase-orders", body);
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create PO");
		return read(response.getBody()).get("id").asLong();
	}

	private void approve(String token, String url) {
		ResponseEntity<String> response = post(token, url, Map.of());
		assertEquals(HttpStatus.OK, response.getStatusCode(), "approve " + url);
	}

	private void reject(String token, String url) {
		ResponseEntity<String> response = post(token, url, Map.of("reason", "tidak sesuai"));
		assertEquals(HttpStatus.OK, response.getStatusCode(), "reject " + url);
	}

	private long createGrn(String token, long poId, long warehouseId, List<Map<String, Object>> lines) {
		Map<String, Object> body = Map.of(
				"purchaseOrderId", poId,
				"warehouseId", warehouseId,
				"receiptDate", "2026-11-23",
				"lines", lines);
		ResponseEntity<String> response = post(token, "/api/goods-receipts", body);
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create GRN");
		return read(response.getBody()).get("id").asLong();
	}

	private long createSo(String token, long customerId, List<Map<String, Object>> lines) {
		Map<String, Object> body = Map.of(
				"customerId", customerId,
				"orderDate", "2026-11-23",
				"lines", lines);
		ResponseEntity<String> response = post(token, "/api/sales-orders", body);
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create SO");
		return read(response.getBody()).get("id").asLong();
	}

	private long createDelivery(String token, long soId, long warehouseId, List<Map<String, Object>> lines) {
		Map<String, Object> body = new HashMap<>();
		body.put("salesOrderId", soId);
		body.put("warehouseId", warehouseId);
		body.put("deliveryDate", "2026-11-23");
		body.put("lines", lines);
		ResponseEntity<String> response = post(token, "/api/deliveries", body);
		assertEquals(HttpStatus.CREATED, response.getStatusCode(), "create delivery");
		return read(response.getBody()).get("id").asLong();
	}

	private Map<String, Object> qtyLine(long itemId, String qty) {
		return Map.of("itemId", itemId, "qty", qty);
	}

	private BigDecimal stockQty(String token, long itemId) {
		ResponseEntity<String> response = get(token, "/api/stock/" + itemId);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "stock query");
		return new BigDecimal(read(response.getBody()).get("totalQty").asString());
	}

	// ---------- auth & roles ----------

	@Test
	void loginAndMeReturnRole() {
		String token = login("staff", "Staff123!");
		ResponseEntity<String> me = get(token, "/api/auth/me");
		assertEquals(HttpStatus.OK, me.getStatusCode());
		assertEquals("STAFF", read(me.getBody()).get("role").asString());
	}

	@Test
	void unauthenticatedRequestsAreRejected() {
		ResponseEntity<String> response = rest.getForEntity("/api/items", String.class);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		JsonNode body = read(response.getBody());
		assertEquals("https://minierp.local/problems/unauthorized", body.get("type").asString());
		assertEquals("/api/items", body.get("instance").asString());
	}

	@Test
	void staffCannotWriteMasterData() {
		String staff = login("staff", "Staff123!");
		String admin = login("admin", "Admin123!");

		ResponseEntity<String> denied = post(staff, "/api/items",
				Map.of("sku", "SKU-DENIED", "name", "x", "uom", "PCS"));
		assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());

		long itemId = createItem(admin);
		assertTrue(itemId > 0);

		// but staff may read master data
		assertEquals(HttpStatus.OK, get(staff, "/api/items").getStatusCode());
	}

	// ---------- purchase flow ----------

	@Test
	void poGrnFlowIncreasesStock() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "10", "100000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");

		long grnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "10")));
		approve(staff, "/api/goods-receipts/" + grnId + "/approve");

		assertEquals(0, stockQty(staff, itemId).compareTo(new BigDecimal("10")));

		// stock card records the movement
		ResponseEntity<String> movements = get(staff, "/api/stock/movements?itemId=" + itemId);
		assertEquals(HttpStatus.OK, movements.getStatusCode());
		JsonNode entries = read(movements.getBody()).get("content");
		assertTrue(entries.size() >= 1);
		assertEquals("IN", entries.get(0).get("movementType").asString());
		assertEquals("GRN", entries.get(0).get("refType").asString());
	}

	@Test
	void highValuePoRequiresManagerApproval() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		// exactly at the threshold: 10.000.000
		long poId = createPo(staff, supplierId, List.of(line(itemId, "10000", "1000")));

		ResponseEntity<String> denied = post(staff, "/api/purchase-orders/" + poId + "/approve", Map.of());
		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, denied.getStatusCode());
		assertTrue(denied.getBody().contains("Admin atau Manager"));

		approve(login("manager", "Manager123!"), "/api/purchase-orders/" + poId + "/approve");
	}

	@Test
	void rejectedPoBlocksGrn() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "5", "1000")));
		reject(login("manager", "Manager123!"), "/api/purchase-orders/" + poId + "/reject");

		ResponseEntity<String> response = post(staff, "/api/goods-receipts",
				Map.of("purchaseOrderId", poId, "warehouseId", warehouseId,
						"receiptDate", "2026-11-23", "lines", List.of(qtyLine(itemId, "5"))));
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	}

	@Test
	void rejectedGrnAllowsRecreatingGrn() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "5", "1000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");

		long grnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "5")));
		reject(staff, "/api/goods-receipts/" + grnId + "/reject");

		// PO is still APPROVED, so a new GRN can be created and approved
		long secondGrnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "5")));
		approve(staff, "/api/goods-receipts/" + secondGrnId + "/approve");
		assertEquals(0, stockQty(staff, itemId).compareTo(new BigDecimal("5")));
	}

	@Test
	void grnQtyCannotExceedPoQty() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "5", "1000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");
		createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "5")));

		ResponseEntity<String> response = post(staff, "/api/goods-receipts",
				Map.of("purchaseOrderId", poId, "warehouseId", warehouseId,
						"receiptDate", "2026-11-23", "lines", List.of(qtyLine(itemId, "1"))));
		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
		assertTrue(response.getBody().contains("melebihi qty pesanan"));
	}

	// ---------- sales flow ----------

	@Test
	void soDeliveryFlowDecreasesStock() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long customerId = createCustomer(admin);
		long warehouseId = createWarehouse(admin);

		// stock in: PO -> GRN 10 pcs
		long poId = createPo(staff, supplierId, List.of(line(itemId, "10", "1000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");
		long grnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "10")));
		approve(staff, "/api/goods-receipts/" + grnId + "/approve");

		// stock out: SO -> Delivery 4 pcs
		long soId = createSo(staff, customerId, List.of(line(itemId, "4", "1500")));
		approve(staff, "/api/sales-orders/" + soId + "/approve");
		long deliveryId = createDelivery(staff, soId, warehouseId, List.of(qtyLine(itemId, "4")));
		approve(staff, "/api/deliveries/" + deliveryId + "/approve");

		assertEquals(0, stockQty(staff, itemId).compareTo(new BigDecimal("6")));

		ResponseEntity<String> movements = get(staff, "/api/stock/movements?itemId=" + itemId);
		JsonNode entries = read(movements.getBody()).get("content");
		assertEquals("OUT", entries.get(0).get("movementType").asString());
		assertEquals("DELIVERY", entries.get(0).get("refType").asString());
	}

	@Test
	void deliveryRequiresEnoughStock() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long customerId = createCustomer(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "3", "1000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");
		long grnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "3")));
		approve(staff, "/api/goods-receipts/" + grnId + "/approve");

		long soId = createSo(staff, customerId, List.of(line(itemId, "5", "1500")));
		approve(staff, "/api/sales-orders/" + soId + "/approve");

		// stock 3 < qty pesanan 5
		ResponseEntity<String> response = post(staff, "/api/deliveries",
				Map.of("salesOrderId", soId, "warehouseId", warehouseId,
						"deliveryDate", "2026-11-23", "lines", List.of(qtyLine(itemId, "5"))));
		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
		assertTrue(response.getBody().contains("Stok tidak mencukupi"));
	}

	@Test
	void deliveryQtyCannotExceedSoQty() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long customerId = createCustomer(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "10", "1000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");
		long grnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "10")));
		approve(staff, "/api/goods-receipts/" + grnId + "/approve");

		long soId = createSo(staff, customerId, List.of(line(itemId, "5", "1500")));
		approve(staff, "/api/sales-orders/" + soId + "/approve");

		ResponseEntity<String> response = post(staff, "/api/deliveries",
				Map.of("salesOrderId", soId, "warehouseId", warehouseId,
						"deliveryDate", "2026-11-23", "lines", List.of(qtyLine(itemId, "6"))));
		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
		assertTrue(response.getBody().contains("melebihi qty pesanan"));
	}

	@Test
	void rejectedSoBlocksDelivery() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long customerId = createCustomer(admin);
		long warehouseId = createWarehouse(admin);

		long soId = createSo(staff, customerId, List.of(line(itemId, "5", "1500")));
		reject(login("manager", "Manager123!"), "/api/sales-orders/" + soId + "/reject");

		ResponseEntity<String> response = post(staff, "/api/deliveries",
				Map.of("salesOrderId", soId, "warehouseId", warehouseId,
						"deliveryDate", "2026-11-23", "lines", List.of(qtyLine(itemId, "5"))));
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	}

	@Test
	void rejectedDeliveryAllowsRecreatingDelivery() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");

		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long customerId = createCustomer(admin);
		long warehouseId = createWarehouse(admin);

		long poId = createPo(staff, supplierId, List.of(line(itemId, "5", "1000")));
		approve(staff, "/api/purchase-orders/" + poId + "/approve");
		long grnId = createGrn(staff, poId, warehouseId, List.of(qtyLine(itemId, "5")));
		approve(staff, "/api/goods-receipts/" + grnId + "/approve");

		long soId = createSo(staff, customerId, List.of(line(itemId, "5", "1500")));
		approve(staff, "/api/sales-orders/" + soId + "/approve");

		long deliveryId = createDelivery(staff, soId, warehouseId, List.of(qtyLine(itemId, "5")));
		reject(staff, "/api/deliveries/" + deliveryId + "/reject");

		// SO is still APPROVED, so a new delivery can be created and approved
		long secondDeliveryId = createDelivery(staff, soId, warehouseId, List.of(qtyLine(itemId, "5")));
		approve(staff, "/api/deliveries/" + secondDeliveryId + "/approve");
		assertEquals(0, stockQty(staff, itemId).compareTo(BigDecimal.ZERO));
	}

	// ---------- response contract ----------

	@Test
	void listReturnsPaginationEnvelope() {
		String admin = login("admin", "Admin123!");
		long itemId = createItem(admin);

		ResponseEntity<String> response = get(admin, "/api/items?page=0&size=1&sort=sku,asc");
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JsonNode body = read(response.getBody());
		assertTrue(itemId > 0);
		assertTrue(body.get("content").isArray());
		assertTrue(body.get("content").size() <= 1);
		assertEquals(0, body.get("page").asInt());
		assertEquals(1, body.get("size").asInt());
		assertTrue(body.get("totalElements").asLong() >= 1);
		assertTrue(body.get("totalPages").asInt() >= 1);
	}

	@Test
	void listClampsOversizedPageRequest() {
		String admin = login("admin", "Admin123!");
		ResponseEntity<String> response = get(admin, "/api/items?size=10000");
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(PaginationDefaults.MAX_SIZE, read(response.getBody()).get("size").asInt());
	}

	@Test
	void listRejectsUnknownSortProperty() {
		String admin = login("admin", "Admin123!");
		ResponseEntity<String> response = get(admin, "/api/items?sort=hacker,asc");
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode body = read(response.getBody());
		assertEquals("https://minierp.local/problems/invalid-sort", body.get("type").asString());
		assertEquals("/api/items", body.get("instance").asString());
	}

	@Test
	void validationFailureReturnsFieldErrors() {
		String admin = login("admin", "Admin123!");
		ResponseEntity<String> response = post(admin, "/api/items", Map.of("name", "Tanpa SKU"));
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getHeaders().getContentType().toString().contains("problem"));
		JsonNode body = read(response.getBody());
		assertEquals("https://minierp.local/problems/validation-failed", body.get("type").asString());
		assertEquals("/api/items", body.get("instance").asString());
		JsonNode errors = body.get("errors");
		assertTrue(errors.isArray());
		List<String> fields = new ArrayList<>();
		errors.forEach(error -> fields.add(error.get("field").asString()));
		assertTrue(fields.contains("sku"));
		assertTrue(fields.contains("uom"));
	}

	@Test
	void malformedJsonReturnsBadRequest() {
		String admin = login("admin", "Admin123!");
		ResponseEntity<String> response = rest.exchange("/api/items", HttpMethod.POST,
				new HttpEntity<>("{not-json", auth(admin)), String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("https://minierp.local/problems/malformed-request",
				read(response.getBody()).get("type").asString());
	}

	@Test
	void unsupportedMethodReturnsMethodNotAllowed() {
		String admin = login("admin", "Admin123!");
		ResponseEntity<String> response = rest.exchange("/api/items", HttpMethod.PUT,
				new HttpEntity<>(Map.of(), auth(admin)), String.class);
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertEquals("https://minierp.local/problems/method-not-allowed",
				read(response.getBody()).get("type").asString());
	}

	@Test
	void unknownRouteReturnsNotFoundProblem() {
		String admin = login("admin", "Admin123!");
		ResponseEntity<String> response = get(admin, "/api/does-not-exist");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		JsonNode body = read(response.getBody());
		assertEquals("https://minierp.local/problems/not-found", body.get("type").asString());
		assertEquals("/api/does-not-exist", body.get("instance").asString());
	}

	// ---------- api versioning ----------

	@Test
	void apiVersionHeaderIsValidated() {
		String admin = login("admin", "Admin123!");
		HttpHeaders headers = auth(admin);
		headers.set("X-API-Version", "2.0");
		ResponseEntity<String> response = rest.exchange("/api/items", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		JsonNode body = read(response.getBody());
		assertEquals("https://minierp.local/problems/invalid-api-version", body.get("type").asString());
		assertTrue(body.get("detail").asString().contains("2.0"));
	}

	@Test
	void apiVersionIsOptionalAndSupportedVersionIsAccepted() {
		String admin = login("admin", "Admin123!");
		assertEquals(HttpStatus.OK, get(admin, "/api/items").getStatusCode());

		HttpHeaders headers = auth(admin);
		headers.set("X-API-Version", "1.0");
		assertEquals(HttpStatus.OK, rest.exchange("/api/items", HttpMethod.GET,
				new HttpEntity<>(headers), String.class).getStatusCode());
	}

	// ---------- problem catalog ----------

	@Test
	void problemCatalogIsPublicAndComplete() {
		ResponseEntity<String> response = rest.getForEntity("/api/problems", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JsonNode catalog = read(response.getBody());
		assertTrue(catalog.isArray());
		assertEquals(ProblemCatalog.all().size(), catalog.size());
		List<String> slugs = new ArrayList<>();
		catalog.forEach(entry -> slugs.add(entry.get("slug").asString()));
		for (String slug : List.of(ProblemTypes.NOT_FOUND, ProblemTypes.VALIDATION_FAILED,
				ProblemTypes.INVALID_SORT, ProblemTypes.INVALID_API_VERSION, ProblemTypes.INTERNAL_ERROR)) {
			assertTrue(slugs.contains(slug), "catalog missing " + slug);
		}
	}

	@Test
	void problemCatalogReturnsDescriptorAnd404ForUnknownSlug() {
		ResponseEntity<String> response = rest.getForEntity("/api/problems/not-found", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		JsonNode body = read(response.getBody());
		assertEquals("not-found", body.get("slug").asString());
		assertEquals(404, body.get("status").asInt());

		ResponseEntity<String> unknown = rest.getForEntity("/api/problems/unknown-slug", String.class);
		assertEquals(HttpStatus.NOT_FOUND, unknown.getStatusCode());
		assertEquals("https://minierp.local/problems/not-found", read(unknown.getBody()).get("type").asString());
	}

	// ---------- correlation id ----------

	@Test
	void correlationIdIsEchoedAndReportedOnErrors() {
		String admin = login("admin", "Admin123!");
		HttpHeaders headers = auth(admin);
		headers.set("X-Correlation-Id", "test-corr-1");

		ResponseEntity<String> ok = rest.exchange("/api/items", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertEquals("test-corr-1", ok.getHeaders().getFirst("X-Correlation-Id"));

		ResponseEntity<String> error = rest.exchange("/api/items/999999999", HttpMethod.GET,
				new HttpEntity<>(headers), String.class);
		assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
		assertEquals("test-corr-1", error.getHeaders().getFirst("X-Correlation-Id"));
		assertEquals("test-corr-1", read(error.getBody()).get("correlationId").asString());
	}

	@Test
	void correlationIdIsGeneratedWhenAbsent() {
		String admin = login("admin", "Admin123!");
		String correlationId = get(admin, "/api/items").getHeaders().getFirst("X-Correlation-Id");
		assertNotNull(correlationId);
		assertTrue(correlationId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
				correlationId);
	}

	// ---------- locked date format ----------

	@Test
	void datesUseLockedIsoFormat() {
		String admin = login("admin", "Admin123!");
		String staff = login("staff", "Staff123!");
		long itemId = createItem(admin);
		long supplierId = createSupplier(admin);
		long poId = createPo(staff, supplierId, List.of(line(itemId, "1", "1000")));

		JsonNode body = read(get(staff, "/api/purchase-orders/" + poId).getBody());
		assertEquals("2026-11-23", body.get("orderDate").asString());
		assertTrue(body.get("createdAt").asString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"),
				body.get("createdAt").asString());
	}
}
