package com.mikelekan.artgallery.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mikelekan.artgallery.model.ArtWork;
import com.mikelekan.artgallery.model.Customer;
import com.mikelekan.artgallery.model.Order;
import com.mikelekan.artgallery.model.OrderStatus;
import com.mikelekan.artgallery.repository.OrderRepository;
import com.mikelekan.artgallery.repository.ArtWorkRepository;
import com.mikelekan.artgallery.service.vendors.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ArtWorkRepository artWorkRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;
    private List<Customer> mockCustomers;
    private List<Order> mockOrders;

    @BeforeEach
    public void setUp()
    {
        // Initialize test data before each test
        mockCustomers = getMockCustomers();
        mockOrders = getMockOrders(mockCustomers);
    }

    @Test
    void testMarkAsPaid_Success() {
        // ... your test code here
    }

    @Test
    void markAsPaid_ShouldUpdateStatusToPaid() {
        // 1. Arrange (Set up the scenario)
        Long orderId = 46L;
        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // 2. Act (Call the actual method)
        orderService.markAsPaid(orderId);

        // 3. Assert (Verify the results)
        assertEquals(OrderStatus.PAID, mockOrder.getStatus());

        // Verify that the repository's save method was actually called
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test void getCustomersBasedOnPrice()
    {
        List<Order> result = mockOrders.stream()
                .sorted(Comparator.comparing(Order::getPrice))
                .toList();
    }
    @Test
    void customerWithMostOrdersList() {
        List<Customer> topCustomers = mockOrders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Customer, Long>comparingByValue().reversed()) // Sort highest to lowest
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println(topCustomers);
    }

    @Test
    void customerWithMostOrders() {
        Customer result = mockOrders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Assert that John has the most orders (he has 2)
        assert result != null;
        assertEquals("Doe", result.getLastName());
        // or if using the list approach:
        // assertEquals(mockCustomers.get(0), result);
    }

    @Test
    void customerWithLeastOrders()
    {
        Customer result = mockOrders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.counting()))
                .entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        System.out.println(result);
    }

    @Test
    void countCustomersByState()
    {
        LinkedHashMap<String, Long> result = mockOrders.stream()
                .map(Order::getCustomer)
                .collect(Collectors.groupingBy(Customer::getState, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println();
    }

    @Test
    void customerTotals()
    {
        LinkedHashMap<String, BigDecimal> result = mockOrders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.reducing(
                        BigDecimal.ZERO,
                        Order::getPrice,
                        BigDecimal::add)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Customer, BigDecimal>comparingByValue().reversed())  // ← ADD THIS
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getFirstName() + " " + entry.getKey().getLastName(),  // ← CHANGE THIS
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));

        System.out.println();
    }

    // Keep your helper methods as private methods
    private List<Customer> getMockCustomers()
    {
        Customer c1 = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .addressLine1("123 Any Street")
                .city("Denver")
                .state("CO")
                .zipCode("80228")
                .email("john@me.com")
                .build();

        Customer c2 = Customer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .addressLine1("123 Smith Street")
                .city("Denver")
                .state("CO")
                .zipCode("80228")
                .email("jane@me.com")
                .build();

        Customer c3 = Customer.builder()
                .id(3L)
                .firstName("Bob")
                .lastName("Ross")
                .addressLine1("123 Happy Tree Street")
                .city("Tallahassee")
                .state("FL")
                .zipCode("80228")
                .email("bob@art.com")
                .build();

        return List.of(c1, c2, c3);
    }

    private List<Order> getMockOrders(List<Customer> customers) {
        ArtWork a1 = ArtWork.builder()
                .id(101L)
                .title("Starry Night")
                .price(new BigDecimal("1200.00"))
                .sold(true)
                .build();
        ArtWork a2 = ArtWork.builder()
                .id(102L)
                .title("Sunflowers")
                .price(new BigDecimal("850.00"))
                .sold(true).build();

        ArtWork a3 = ArtWork.builder()
                .id(103L)
                .title("The Scream")
                .price(new BigDecimal("2500.00"))
                .sold(true)
                .build();

        ArtWork a4 = ArtWork.builder()
                .id(104L)
                .title("The Small Deal")
                .price(new BigDecimal("200.00"))
                .sold(true)
                .build();

        ArtWork a5 = ArtWork.builder()
                .id(105L)
                .title("Secret Portrait")
                .price(new BigDecimal("2300.00"))
                .sold(true)
                .build();

        ArtWork a6= ArtWork.builder()
                .id(106L)
                .title("A Big Deal")
                .price(new BigDecimal("2900.00"))
                .sold(true)
                .build();

        return List.of(
                Order.builder()
                        .id(1L)
                        .customer(customers.get(0))
                        .artwork(a1)
                        .price(a1.getPrice())
                        .build(),
                Order.builder()
                        .id(2L)
                        .customer(customers.get(0))
                        .artwork(a2)
                        .price(a2.getPrice())
                        .build(),
                Order.builder()
                        .id(3L)
                        .customer(customers.get(1))
                        .artwork(a3)
                        .price(a3.getPrice())
                        .build(),

                Order.builder()
                        .id(4L)
                        .customer(customers.get(2))
                        .artwork(a5)
                        .price(a5.getPrice())
                        .build()
        );
    }
}