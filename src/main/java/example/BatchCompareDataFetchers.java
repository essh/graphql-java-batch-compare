package example;

import com.google.common.collect.ImmutableMap;
import graphql.execution.batched.Batched;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BatchCompareDataFetchers {
    // Shops
    private static final Map<String, Shop> shops = ImmutableMap.<String, Shop>builder()
            .put("shop-1", new Shop("shop-1", "Shop 1", Arrays.asList("department-1", "department-2", "department-3")))
            .put("shop-2", new Shop("shop-2", "Shop 2", Arrays.asList("department-4", "department-5", "department-6")))
            .put("shop-3", new Shop("shop-3", "Shop 3", Arrays.asList("department-7", "department-8", "department-9")))
            .build();

    public static DataFetcher<List<Shop>> shopsDataFetcher = environment -> new ArrayList<>(shops.values());

    // Departments
    private static Map<String, Department> departments = ImmutableMap.<String, Department>builder()
            .put("department-1", new Department("department-1", "Department 1", Arrays.asList("product-1")))
            .put("department-2", new Department("department-2", "Department 2", Arrays.asList("product-2")))
            .put("department-3", new Department("department-3", "Department 3", Arrays.asList("product-3")))
            .put("department-4", new Department("department-4", "Department 4", Arrays.asList("product-4")))
            .put("department-5", new Department("department-5", "Department 5", Arrays.asList("product-5")))
            .put("department-6", new Department("department-6", "Department 6", Arrays.asList("product-6")))
            .put("department-7", new Department("department-7", "Department 7", Arrays.asList("product-7")))
            .put("department-8", new Department("department-8", "Department 8", Arrays.asList("product-8")))
            .put("department-9", new Department("department-9", "Department 9", Arrays.asList("product-9")))
            .build();

    private static List<Department> getDepartmentsForShop(Shop shop) {
        return shop.getDepartmentIds().stream().map(id -> departments.get(id)).collect(Collectors.toList());
    }

    private static List<List<Department>> getDepartmentsForShops(List<Shop> shops) {
        System.out.println("getDepartmentsForShops batch: " + shops);
        return shops.stream().map(BatchCompareDataFetchers::getDepartmentsForShop).collect(Collectors.toList());
    }

    public static DataFetcher<List<List<Department>>> departmentsForShopsBatchedDataFetcher = new DataFetcher<List<List<Department>>>() {
        @Override
        @Batched
        public List<List<Department>> get(DataFetchingEnvironment environment) {
            List<Shop> shops = environment.getSource();
            return getDepartmentsForShops(shops);
        }
    };

    private static BatchLoader<String, List<Department>> departmentsForShopsBatchLoader = ids -> {
        List<Shop> s = ids.stream().map(shops::get).collect(Collectors.toList());
        return CompletableFuture.completedFuture(getDepartmentsForShops(s));
    };

    public static DataLoader<String, List<Department>> departmentsForShopDataLoader = new DataLoader<>(departmentsForShopsBatchLoader);

    public static DataFetcher<CompletableFuture<List<Department>>> departmentsForShopDataLoaderDataFetcher = environment -> {
        Shop shop = environment.getSource();
        return departmentsForShopDataLoader.load(shop.getId());
    };

    // Products
    private static Map<String, Product> products = ImmutableMap.<String, Product>builder()
            .put("product-1", new Product("product-1", "Product 1"))
            .put("product-2", new Product("product-2", "Product 2"))
            .put("product-3", new Product("product-3", "Product 3"))
            .put("product-4", new Product("product-4", "Product 4"))
            .put("product-5", new Product("product-5", "Product 5"))
            .put("product-6", new Product("product-6", "Product 6"))
            .put("product-7", new Product("product-7", "Product 7"))
            .put("product-8", new Product("product-8", "Product 8"))
            .put("product-9", new Product("product-9", "Product 9"))
            .build();

    private static List<Product> getProductsForDepartment(Department department) {
        return department.getProductIds().stream().map(id -> products.get(id)).collect(Collectors.toList());
    }

    private static List<List<Product>> getProductsForDepartments(List<Department> departments) {
        System.out.println("getProductsForDepartments batch: " + departments);
        return departments.stream().map(BatchCompareDataFetchers::getProductsForDepartment).collect(Collectors.toList());
    }

    public static DataFetcher<List<List<Product>>> productsForDepartmentsBatchedDataFetcher = new DataFetcher<List<List<Product>>>() {
        @Override
        @Batched
        public List<List<Product>> get(DataFetchingEnvironment environment) {
            List<Department> departments = environment.getSource();
            return getProductsForDepartments(departments);
        }
    };

    private static BatchLoader<String, List<Product>> productsForDepartmentsBatchLoader = ids -> {
        List<Department> d = ids.stream().map(departments::get).collect(Collectors.toList());
        return CompletableFuture.completedFuture(getProductsForDepartments(d));
    };

    public static DataLoader<String, List<Product>> productsForDepartmentDataLoader = new DataLoader<>(productsForDepartmentsBatchLoader);

    public static DataFetcher<CompletableFuture<List<Product>>> productsForDepartmentDataLoaderDataFetcher = environment -> {
        Department department = environment.getSource();
        return productsForDepartmentDataLoader.load(department.getId());
    };
}
