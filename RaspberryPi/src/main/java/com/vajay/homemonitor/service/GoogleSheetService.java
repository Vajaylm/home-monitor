package com.vajay.homemonitor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vajay.homemonitor.model.WeatherData;
import com.vajay.homemonitor.util.DateTimeUtil;

@Service
public class GoogleSheetService {
    private static final String APPLICATION_NAME = "home-monitor";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = System.getenv().get("secretFilePath");
    private static final String SPREADSHEET_ID = System.getenv().get("spreadsheetId");
    private static final String RANGE = System.getenv().get("spreadsheetSheet");
    private WeatherData outWeatherData;
    private WeatherData inWeatherData;

    public GoogleSheetService(WeatherData outWeatherData, WeatherData inWeatherData) {
        this.outWeatherData = outWeatherData;
        this.inWeatherData = inWeatherData;
    }

    private Credential getCredentials() throws IOException, GeneralSecurityException {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public void appendData() throws GeneralSecurityException, IOException {
        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
                getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange body = new ValueRange()
                .setValues(Arrays.asList(Arrays.asList(DateTimeUtil.getDate(), DateTimeUtil.getTime(),
                        outWeatherData.getTemperature(), outWeatherData.getHumidity(),
                        outWeatherData.getPressure(), inWeatherData.getTemperature(), inWeatherData.getHumidity(),
                        inWeatherData.getPressure())));
        service.spreadsheets().values().append(SPREADSHEET_ID, RANGE, body).setValueInputOption("RAW").execute();
    }
}
