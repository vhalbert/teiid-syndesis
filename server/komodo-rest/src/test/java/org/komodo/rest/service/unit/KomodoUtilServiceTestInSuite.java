/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.rest.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.komodo.importer.ImportMessages;
import org.komodo.rest.KomodoRestV1Application.V1Constants;
import org.komodo.rest.KomodoService;
import org.komodo.rest.cors.CorsHeaders;
import org.komodo.rest.relational.RelationalMessages;
import org.komodo.rest.relational.json.KomodoJsonMarshaller;
import org.komodo.rest.relational.response.KomodoStatusObject;
import org.komodo.rest.service.KomodoUtilService;
import org.komodo.spi.repository.KomodoType;
import org.komodo.test.utils.TestUtilities;

@SuppressWarnings( {"javadoc", "nls"} )
public class KomodoUtilServiceTestInSuite extends AbstractKomodoServiceTest {

    public KomodoUtilServiceTestInSuite() throws Exception {
        super();
    }

    private void loadSamples(String user) throws Exception {
        for (String sample : KomodoUtilService.SAMPLES) {
            ImportMessages msgs = restApp().importVdb(KomodoUtilService.getVdbSample(sample), user);
            assertTrue(msgs.getErrorMessages().isEmpty());
        }
    }

    @Test
    public void shouldAbout() throws Exception {
        String[] EXPECTED = {
            OPEN_BRACE + NEW_LINE,
            "  \"Information\": " +  OPEN_BRACE + NEW_LINE,
            "    \"Repository Workspace\": \"komodoLocalWorkspace\"," + NEW_LINE,
            "    \"Repository Configuration\"", // Configuration Url contains local file names so impossible to test
            "    \"Repository Vdb Total\": \"",
            "  " + CLOSE_BRACE + NEW_LINE };

        loadSamples(USER_NAME);

        // get
        URI uri = UriBuilder.fromUri(uriBuilder().baseUri())
                                                    .path(V1Constants.SERVICE_SEGMENT)
                                                    .path(V1Constants.ABOUT).build();

        HttpGet request = jsonRequest(uri, RequestType.GET);
        addHeader(request, CorsHeaders.ORIGIN, "http://localhost:2772");
        HttpResponse response = executeOk(request);

        String entity = extractResponse(response);
//        System.out.println("Response from uri " + uri + ":\n" + entity);
        for (String expected : EXPECTED) {
            assertTrue(entity.contains(expected));
        }

        // This are generated on build from maven variables so check they are listed
        assertTrue(entity.contains(KomodoUtilService.APP_NAME));
        assertTrue(entity.contains(KomodoUtilService.APP_TITLE));
        assertTrue(entity.contains(KomodoUtilService.APP_VERSION));
        assertTrue(entity.contains(KomodoUtilService.APP_DESCRIPTION));
    }

    @Test
    public void shouldGetUserProfile() throws Exception {
        String[] EXPECTED = {
            OPEN_BRACE + NEW_LINE,
            "  \"Information\": " +  OPEN_BRACE + NEW_LINE,
            "    \"" + KomodoUtilService.USER_NAME + "\": \"" + USER_NAME + "\"," + NEW_LINE,
            "    \"" + KomodoUtilService.WORKSPACE + "\": \"" + serviceTestUtilities.getWorkspace(USER_NAME) + "\"," + NEW_LINE, // Configuration Url contains local file names so impossible to test
            "    \"Repository Vdb Total\": \"",
            "  " + CLOSE_BRACE + NEW_LINE };

        // get
        URI uri = UriBuilder.fromUri(uriBuilder().baseUri())
                                                    .path(V1Constants.SERVICE_SEGMENT)
                                                    .path(V1Constants.USER_PROFILE).build();

        HttpGet request = jsonRequest(uri, RequestType.GET);
        addHeader(request, CorsHeaders.ORIGIN, "http://localhost:2772");
        HttpResponse response = executeOk(request);

        String entity = extractResponse(response);
        System.out.println("Response from uri " + uri + ":\n" + entity);
        for (String expected : EXPECTED) {
            System.out.println("Expected " + expected);
            assertTrue(entity.contains(expected));
        }

        // This are generated on build from maven variables so check they are listed
        assertTrue(entity.contains(KomodoUtilService.USER_NAME));
        assertTrue(entity.contains(KomodoUtilService.WORKSPACE));
    }

    @Test
    public void shouldLoadSampleData() throws Exception {

        try {
            serviceTestUtilities.deleteVdbs(USER_NAME);

            // get
            URI uri = UriBuilder.fromUri(uriBuilder().baseUri()).path(V1Constants.SERVICE_SEGMENT).path(V1Constants.SAMPLE_DATA).build();

            HttpPost request = jsonRequest(uri, RequestType.POST);
            HttpResponse response = executeOk(request);

            String entity = extractResponse(response);
            // System.out.println("Response from uri " + uri + ":\n" + entity);

            KomodoStatusObject status = KomodoJsonMarshaller.unmarshall(entity, KomodoStatusObject.class);
            assertNotNull(status);

            assertEquals("Sample Vdb Import", status.getTitle());
            Map<String, String> attributes = status.getAttributes();

            for (String sample : KomodoUtilService.SAMPLES) {
                String message = attributes.get(sample);
                assertNotNull(message);
                assertTrue(message.startsWith("The sample vdb"));
            }
        } finally {
            //
            // Restore the sample vdbs
            //
            unitServiceResources.loadVdbs();            
        }
    }

    @Test
    public void shouldLoadSamplesDataAlreadyExists() throws Exception {
        loadSamples(USER_NAME);

        // get
        URI uri = UriBuilder.fromUri(uriBuilder().baseUri())
                                                    .path(V1Constants.SERVICE_SEGMENT)
                                                    .path(V1Constants.SAMPLE_DATA).build();

        HttpPost request = jsonRequest(uri, RequestType.POST);
        HttpResponse response = executeOk(request);

        String entity = extractResponse(response);
        // System.out.println("Response from uri " + uri + ":\n" + entity);

        KomodoStatusObject status = KomodoJsonMarshaller.unmarshall(entity, KomodoStatusObject.class);
        assertNotNull(status);

        assertEquals("Sample Vdb Import", status.getTitle());
        Map<String, String> attributes = status.getAttributes();

        for (String sample : KomodoUtilService.SAMPLES) {
            String message = attributes.get(sample);
            assertNotNull(message);

            assertEquals(
                         RelationalMessages.getString(
                                                      RelationalMessages.Error.VDB_SAMPLE_IMPORT_VDB_EXISTS,
                                                      sample), message);
        }
    }

    @Test
    public void shouldReturnTeiidSchema() throws Exception {

        // get
        URI uri = UriBuilder.fromUri(uriBuilder().baseUri())
                                                    .path(V1Constants.SERVICE_SEGMENT)
                                                    .path(V1Constants.SCHEMA_SEGMENT)
                                                    .build();

        HttpGet request = jsonRequest(uri, RequestType.GET);
        HttpResponse response = executeOk(request);

        String entity = extractResponse(response);
//        System.out.println("Response from uri " + uri + ":\n" + entity);

        InputStream schemaStream = getClass().getResourceAsStream("teiid-schema.json");
        String expected = TestUtilities.toString(schemaStream);

        if (! expected.trim().equals(entity)) {
            schemaStream = getClass().getResourceAsStream("teiid-schema2.json");
            expected = TestUtilities.toString(schemaStream);
            assertEquals(expected.trim(), entity);
        }
    }

    @Test
    public void shouldReturnTeiidSchemaForKType() throws Exception {

        // get
        UriBuilder baseBuilder = UriBuilder.fromUri(uriBuilder().baseUri())
                                                    .path(V1Constants.SERVICE_SEGMENT)
                                                    .path(V1Constants.SCHEMA_SEGMENT);

        // Get model
        URI uri = baseBuilder.clone()
                                         .queryParam(KomodoService.QueryParamKeys.KTYPE, KomodoType.MODEL)
                                         .build();

        HttpGet request = jsonRequest(uri, RequestType.GET);
        HttpResponse response = executeOk(request);

        String entity = extractResponse(response);
//        System.out.println("Response from uri " + uri + ":\n" + entity);

        assertFalse(entity.contains("\"schema-1\" : {"));
        assertFalse(entity.contains("\"keng__id\" : \"vdb\""));
        assertFalse(entity.contains("\"keng__id\": \"connection\""));
        assertFalse(entity.contains("\"keng__id\" : \"importVdb\""));
        assertTrue(entity.contains("\"keng__id\" : \"model\""));
        assertFalse(entity.contains("\"keng__id\" : \"source\""));
        assertFalse(entity.contains("\"keng__id\" : \"metadata\""));
        assertFalse(entity.contains("\"keng__id\" : \"validationError\""));
        assertFalse(entity.contains("\"keng__id\" : \"translator\""));
        assertFalse(entity.contains("\"keng__id\" : \"dataRole\""));
        assertFalse(entity.contains("\"keng__id\" : \"permission\""));
        assertFalse(entity.contains("\"keng__id\" : \"condition\""));
        assertFalse(entity.contains("\"keng__id\" : \"mask\""));
        assertFalse(entity.contains("\"keng__id\" : \"entry\""));

        // Get data role
        uri = baseBuilder.clone()
                                  .queryParam(KomodoService.QueryParamKeys.KTYPE, KomodoType.VDB_DATA_ROLE)
                                  .build();

        request = jsonRequest(uri, RequestType.GET);
        response = executeOk(request);

        entity = extractResponse(response);

        //System.out.println("Response from uri " + uri + ":\n" + entity);

        assertFalse(entity.contains("\"schema-1\" : {"));
        assertFalse(entity.contains("\"keng__id\" : \"vdb\""));
        assertFalse(entity.contains("\"keng__id\": \"connection\""));
        assertFalse(entity.contains("\"keng__id\" : \"importVdb\""));
        assertFalse(entity.contains("\"keng__id\" : \"model\""));
        assertFalse(entity.contains("\"keng__id\" : \"source\""));
        assertFalse(entity.contains("\"keng__id\" : \"metadata\""));
        assertFalse(entity.contains("\"keng__id\" : \"validationError\""));
        assertFalse(entity.contains("\"keng__id\" : \"translator\""));
        assertTrue(entity.contains("\"keng__id\" : \"dataRole\""));
        assertFalse(entity.contains("\"keng__id\" : \"permission\""));
        assertFalse(entity.contains("\"keng__id\" : \"condition\""));
        assertFalse(entity.contains("\"keng__id\" : \"mask\""));
        assertFalse(entity.contains("\"keng__id\" : \"entry\""));

        // Get mask
        uri = baseBuilder.clone().
                                   queryParam(KomodoService.QueryParamKeys.KTYPE, KomodoType.VDB_MASK)
                                   .build();

        request = jsonRequest(uri, RequestType.GET);
        response = executeOk(request);

        entity = extractResponse(response);
        //System.out.println("Response from uri " + uri + ":\n" + entity);

        assertFalse(entity.contains("\"schema-1\" : {"));
        assertFalse(entity.contains("\"keng__id\" : \"vdb\""));
        assertFalse(entity.contains("\"keng__id\": \"connection\""));
        assertFalse(entity.contains("\"keng__id\" : \"importVdb\""));
        assertFalse(entity.contains("\"keng__id\" : \"model\""));
        assertFalse(entity.contains("\"keng__id\" : \"source\""));
        assertFalse(entity.contains("\"keng__id\" : \"metadata\""));
        assertFalse(entity.contains("\"keng__id\" : \"validationError\""));
        assertFalse(entity.contains("\"keng__id\" : \"translator\""));
        assertFalse(entity.contains("\"keng__id\" : \"dataRole\""));
        assertFalse(entity.contains("\"keng__id\" : \"permission\""));
        assertFalse(entity.contains("\"keng__id\" : \"condition\""));
        assertTrue(entity.contains("\"keng__id\" : \"mask\""));
        assertFalse(entity.contains("\"keng__id\" : \"entry\""));

        // Get data source
        uri = baseBuilder.clone().
                                   queryParam(KomodoService.QueryParamKeys.KTYPE, KomodoType.CONNECTION)
                                   .build();

        request = jsonRequest(uri, RequestType.GET);
        addJsonConsumeContentType(request);
        response = executeOk(request);

        entity = extractResponse(response);
//        System.out.println("Response from uri " + uri + ":\n" + entity);

        assertFalse(entity.contains("\"schema-1\" : {"));
        assertFalse(entity.contains("\"keng__id\" : \"vdb\""));
        assertTrue(entity.contains("\"keng__id\": \"connection\""));
        assertFalse(entity.contains("\"keng__id\" : \"importVdb\""));
        assertFalse(entity.contains("\"keng__id\" : \"model\""));
        assertFalse(entity.contains("\"keng__id\" : \"source\""));
        assertFalse(entity.contains("\"keng__id\" : \"metadata\""));
        assertFalse(entity.contains("\"keng__id\" : \"validationError\""));
        assertFalse(entity.contains("\"keng__id\" : \"translator\""));
        assertFalse(entity.contains("\"keng__id\" : \"dataRole\""));
        assertFalse(entity.contains("\"keng__id\" : \"permission\""));
        assertFalse(entity.contains("\"keng__id\" : \"condition\""));
        assertFalse(entity.contains("\"keng__id\" : \"mask\""));
        assertFalse(entity.contains("\"keng__id\" : \"entry\""));
    }
}
