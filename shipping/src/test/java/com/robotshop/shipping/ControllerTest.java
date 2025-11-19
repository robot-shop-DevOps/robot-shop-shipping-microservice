package com.robotshop.shipping;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Connection;
import javax.sql.DataSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Sort;

@WebMvcTest(controllers = Controller.class)
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityRepository cityrepo;

    @MockBean
    private CodeRepository coderepo;

    @MockBean
    private DataSource dataSource;

    @BeforeEach
    void setup() {
        Controller.bytesGlobal.clear();
    }

    // ===============================
    // /health/liveness
    // ===============================
    @Test
    void testLiveness() {
        Controller controller = new Controller("cart", "8326", dataSource);
        ResponseEntity<String> resp = controller.live();

        assert(resp.getStatusCodeValue() == 200);
        assert("OK".equals(resp.getBody()));
    }

    // ===============================
    // /health/readiness (OK)
    // ===============================
    @Test
    void testReadinessOk() throws Exception {
        Connection conn = Mockito.mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);

        Controller controller = new Controller("cart", "8326", dataSource);
        ResponseEntity<String> resp = controller.ready();

        assert(resp.getStatusCodeValue() == 200);
        assert("OK".equals(resp.getBody()));
    }

    // ===============================
    // /health/readiness (FAIL)
    // ===============================
    @Test
    void testReadinessDbFail() throws Exception {
        when(dataSource.getConnection()).thenThrow(new RuntimeException("DB DOWN"));

        Controller controller = new Controller("cart", "8326", dataSource);
        ResponseEntity<String> resp = controller.ready();

        assert(resp.getStatusCodeValue() == 503);
    }

    // ===============================
    // /count
    // ===============================
    @Test
    void testCount() throws Exception {
        when(cityrepo.count()).thenReturn(5L);

        mockMvc.perform(get("/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    // ===============================
    // /codes
    // ===============================
    @Test
    void testCodes() throws Exception {
        Code c = new Code();
        c.setName("India");

        when(coderepo.findAll(any(Sort.class)))
                .thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("India"));
    }

    // ===============================
    // /cities/{code}
    // ===============================
    @Test
    void testCities() throws Exception {
        City c = new City();
        c.setCity("Hyderabad");

        when(cityrepo.findByCode("IN"))
                .thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/cities/IN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Hyderabad"));
    }

    // ===============================
    // /match/{code}/{text}
    // ===============================
    @Test
    void testMatchValid() throws Exception {
        City c = new City();
        c.setCity("Hyd");

        when(cityrepo.match("IN", "Hyd"))
                .thenReturn(Arrays.asList(c));

        mockMvc.perform(get("/match/IN/Hyd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Hyd"));
    }

    @Test
    void testMatchTooShort() throws Exception {
        mockMvc.perform(get("/match/IN/Hy"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMatchTrimTo9() throws Exception {
        City c = new City();
        c.setCity("City");

        when(cityrepo.match("IN", "Cit"))
                .thenReturn(Collections.nCopies(20, c));

        mockMvc.perform(get("/match/IN/Cit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9));
    }

    // ===============================
    // /calc/{id}
    // ===============================
    @Test
    void testCalcSuccess() throws Exception {
        City c = new City();
        c.setLatitude(10);
        c.setLongitude(20);

        when(cityrepo.findById(1L))
                .thenReturn(Optional.of(c));

        mockMvc.perform(get("/calc/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCalcNotFound() throws Exception {
        when(cityrepo.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/calc/1"))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // /memory
    // ===============================
    @Test
    void testMemoryAddsOnce() throws Exception {
        mockMvc.perform(get("/memory"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // ===============================
    // /free
    // ===============================
    @Test
    void testFreeClearsMemory() throws Exception {
        Controller.bytesGlobal.add(new byte[1024]);

        mockMvc.perform(get("/free"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}