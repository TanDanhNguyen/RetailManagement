
package com.retail.management.integration;

import com.retail.management.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductRbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void deleteProduct_ShouldReturn403_WhenUserIsStaff() throws Exception {

        mockMvc.perform(delete("/api/products/2")
                        .with(user("staff").roles("STAFF")))
                .andExpect(status().isForbidden());
    }
    @Test
    void deleteProduct_ShouldReturn403_WhenUserIsManager() throws Exception {

        mockMvc.perform(delete("/api/products/2")
                        .with(user("manager").roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_ShouldReturn204_WhenUserIsAdmin() throws Exception {

        doNothing().when(productService).deleteProduct(2L);

        mockMvc.perform(delete("/api/products/2")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void getProducts_ShouldReturn200_WhenUserIsStaff() throws Exception {

        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products")
                        .with(user("staff").roles("STAFF"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
