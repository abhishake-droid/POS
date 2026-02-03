package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.PageForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientDtoTest {

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ClientDto clientDto;

    private ClientForm validForm;
    private ClientPojo clientPojo;

    @BeforeEach
    void setUp() {
        validForm = new ClientForm();
        validForm.setName("Test Client");
        validForm.setEmail("test@example.com");
        validForm.setPhone("1234567890");

        clientPojo = new ClientPojo();
        clientPojo.setId("client1");
        clientPojo.setClientId("cl001");
        clientPojo.setName("test client");
        clientPojo.setEmail("test@example.com");
        clientPojo.setPhone("1234567890");
    }

    @Test
    void testCreate_Success() throws ApiException {
        // Given
        when(clientApi.add(any(ClientPojo.class))).thenReturn(clientPojo);

        // When
        ClientData result = clientDto.create(validForm);

        // Then
        assertNotNull(result);
        assertEquals("cl001", result.getClientId());
        verify(clientApi, times(1)).add(any(ClientPojo.class));
    }

    @Test
    void testCreate_InvalidEmail() {
        // Given
        validForm.setEmail("invalid-email");

        // When/Then
        assertThrows(ApiException.class, () -> clientDto.create(validForm));
    }

    @Test
    void testCreate_EmptyName() {
        // Given
        validForm.setName("");

        // When/Then
        assertThrows(ApiException.class, () -> clientDto.create(validForm));
    }

    @Test
    void testCreate_InvalidPhone() {
        // Given
        validForm.setPhone("123"); // Too short

        // When/Then
        assertThrows(ApiException.class, () -> clientDto.create(validForm));
    }

    @Test
    void testGetById_Success() throws ApiException {
        // Given
        when(clientApi.getCheckByClientId("cl001")).thenReturn(clientPojo);

        // When
        ClientData result = clientDto.getById("cl001");

        // Then
        assertNotNull(result);
        assertEquals("cl001", result.getClientId());
        verify(clientApi, times(1)).getCheckByClientId("cl001");
    }

    @Test
    void testGetAll_Success() throws ApiException {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(0);
        pageForm.setSize(10);

        Page<ClientPojo> page = new PageImpl<>(Arrays.asList(clientPojo));
        when(clientApi.getAll(0, 10)).thenReturn(page);

        // When
        Page<ClientData> result = clientDto.getAll(pageForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(clientApi, times(1)).getAll(0, 10);
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        when(clientApi.update(anyString(), any(ClientPojo.class))).thenReturn(clientPojo);

        // When
        ClientData result = clientDto.update("client1", validForm);

        // Then
        assertNotNull(result);
        verify(clientApi, times(1)).update(anyString(), any(ClientPojo.class));
    }

    @Test
    void testCreate_NullName() {
        // Given
        validForm.setName(null);

        // When/Then
        assertThrows(ApiException.class, () -> clientDto.create(validForm));
    }

    @Test
    void testUpdate_InvalidEmail() {
        // Given
        validForm.setEmail("bad-email");

        // When/Then
        assertThrows(ApiException.class, () -> clientDto.update("client1", validForm));
    }

    @Test
    void testGetAll_InvalidPage() {
        // Given
        PageForm pageForm = new PageForm();
        pageForm.setPage(-1);
        pageForm.setSize(10);

        // When/Then
        assertThrows(ApiException.class, () -> clientDto.getAll(pageForm));
    }
}
