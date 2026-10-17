package com.rbdip.bookstore.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CustomerTest {
    @ParameterizedTest
    @CsvSource({"Anna Smith,Anna,Smith", "Anna van der Smith,Anna,van der Smith", "Prince,Prince,''"})
    void splitNamesPreserveThePublicFullName(String full, String first, String last) {
        Customer customer = new Customer(full, "Address", "Phone");
        assertThat(customer.getFirstName()).isEqualTo(first);
        assertThat(customer.getLastName()).isEqualTo(last);
        assertThat(customer.getFullName()).isEqualTo(full);
        assertThat(customer.getAddress()).isEqualTo("Address");
        assertThat(customer.getPhone()).isEqualTo("Phone");
    }
}
