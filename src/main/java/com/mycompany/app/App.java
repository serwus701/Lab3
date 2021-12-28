package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import com.esri.arcgisruntime.mapping.Viewpoint;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class App extends Application {

    MyRunnable userThread = new MyRunnable();
    static MapView mapView;
    StackPane stackPane = new StackPane();
    ArrayList<MyButton> buses = new ArrayList<>();
    ArrayList<MyButton> trams = new ArrayList<>();
    ArrayList<String> busName = new ArrayList<>();
    LinkedList<String> vehicleToShowList = new LinkedList<>();
    boolean doAreaCheck = false;
    double areaCheckPointX = 17.036694;
    double areaCheckPointY = 51.11114;
    int sleepTime = 5000;
    double area = 1;
    Label areaInformation = new Label("Your vehicle is in the area");

    public static void main(String[] args) {

        Application.launch(args);
    }

    boolean isTram(String busNr) {
        if ((busNr.equals("1") || busNr.equals("2") || busNr.equals("3") || busNr.equals("4") || busNr.equals("5") || busNr.equals("6") || busNr.equals("7") || busNr.equals("8") || busNr.equals("9") || busNr.equals("10"))) {
            return true;
        }
        return busNr.equals("11") || busNr.equals("15") || busNr.equals("16") || busNr.equals("17") || busNr.equals("20") || busNr.equals("23") || busNr.equals("31") || busNr.equals("33") || busNr.equals("70") || busNr.equals("74");
    }

    double distanceBetweenPositions(double latitudeGeo1, double longitudeGeo1, double latitudeGeo2, double longitudeGeo2) {
        latitudeGeo1 = latitudeGeo1 / 1.65;
        latitudeGeo2 = latitudeGeo2 / 1.65;
        double p = 0.017453292519943295;
        double a = 0.5 - Math.cos((latitudeGeo2 - latitudeGeo1) * p) / 2 + Math.cos(latitudeGeo1 * p) * Math.cos(latitudeGeo2 * p) * (1 - Math.cos((longitudeGeo2 - longitudeGeo1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(a));
    }

    void addBussesButtons() {
        VBox busVbox1 = new VBox();
        VBox busVbox2 = new VBox();
        VBox busVbox3 = new VBox();

        HBox busBox = new HBox();
        busBox.getChildren().addAll(busVbox1, busVbox2, busVbox3);

        busBox.setStyle("-fx-background-color: #c11111");
        busBox.setSpacing(2);
        busVbox1.setSpacing(2);
        busVbox2.setSpacing(2);
        busVbox3.setSpacing(2);

        VBox tramVbox = new VBox();
        StackPane.setAlignment(busBox, Pos.TOP_LEFT);
        busBox.setMaxSize(stackPane.getMaxWidth() / 8, stackPane.getMaxHeight() / 4);
        StackPane.setAlignment(tramVbox, Pos.TOP_RIGHT);
        tramVbox.setMaxSize(stackPane.getMaxWidth() / 8, stackPane.getMaxHeight() / 4);
        tramVbox.setSpacing(2);
        tramVbox.setStyle("-fx-background-color: #1149c1");
        stackPane.getChildren().add(busBox);
        stackPane.getChildren().add(tramVbox);


        ButtonsManagement myButtons = new ButtonsManagement();
        myButtons.fillBuses();
        myButtons.fillTrams();

        buses = myButtons.getBuses();
        trams = myButtons.getTrams();

        for (int i = 0; i < 27; i++) {
            busVbox1.getChildren().add(buses.get(i).getMyButton());
        }
        for (int i = 27; i < 54; i++) {
            busVbox2.getChildren().add(buses.get(i).getMyButton());
        }
        for (int i = 54; i < 73; i++) {
            busVbox3.getChildren().add(buses.get(i).getMyButton());
        }

        for (MyButton myButton : buses
        ) {
            myButton.getMyButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    myButton.press(myButton.getIsOn(), vehicleToShowList);
                    userThread.refresh();
                }
            });
        }

        for (MyButton myButton : trams
        ) {
            tramVbox.getChildren().add(myButton.getMyButton());
            myButton.getMyButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    myButton.press(myButton.getIsOn(), vehicleToShowList);
                    userThread.refresh();
                }
            });
        }
    }

    void addIntervalButtons() {
        VBox refreshBox = new VBox();
        HBox intervalBox = new HBox();
        Label refreshInstructions = new Label("Choose refresh rate");
        refreshBox.setStyle("-fx-background-color: #e11de8");
        refreshBox.getChildren().add(refreshInstructions);
        refreshBox.getChildren().add(intervalBox);
        stackPane.getChildren().add(refreshBox);
        StackPane.setAlignment(refreshBox, Pos.BOTTOM_CENTER);
        refreshBox.setMaxSize(20, stackPane.getMaxHeight() / 10);
        Button set5 = new Button("5s");
        Button set10 = new Button("10s");
        Button set15 = new Button("15s");
        set5.setMinWidth(40);
        set10.setMinWidth(40);
        set15.setMinWidth(40);

        set5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sleepTime = 5000;
                userThread.refresh();
            }
        });
        set10.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sleepTime = 10000;
                userThread.refresh();
            }
        });
        set15.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sleepTime = 15000;
                userThread.refresh();
            }
        });
        intervalBox.getChildren().addAll(set5, set10, set15);
    }

    void addAreaButtons() {
        VBox areaStuff = new VBox();

        Slider areaSlider = new Slider(0, 2, 1);
        areaSlider.setMaxWidth(360);

        Button areaCheckDescription = new Button("Press it to enable area check. Afterwards use right mouse button");

        EventHandler<? super MouseEvent> mapMouseHandler = (EventHandler<MouseEvent>) event -> {
            area = (double) (((Slider) event.getSource()).getValue());
            if (areaCheckDescription.getStyle().equals("-fx-background-color: #30d24b"))
                userThread.refresh();
        };
        EventHandler<? super KeyEvent> mapKeyHandler = (EventHandler<KeyEvent>) event -> {
            area = (double) (((Slider) event.getSource()).getValue());
            if (areaCheckDescription.getStyle().equals("-fx-background-color: #30d24b"))
                userThread.refresh();
        };

        areaSlider.setOnMouseReleased(mapMouseHandler);
        areaSlider.setOnKeyReleased(mapKeyHandler);


        stackPane.getChildren().add(areaStuff);
        StackPane.setAlignment(areaStuff, Pos.TOP_CENTER);
        areaStuff.setMaxWidth(400);
        areaStuff.setMaxHeight(40);

        areaCheckDescription.setStyle("-fx-background-color: #d29f30");

        areaCheckDescription.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doAreaCheck = !doAreaCheck;
                if (areaCheckDescription.getStyle().equals("-fx-background-color: #d29f30"))
                    areaCheckDescription.setStyle("-fx-background-color: #30d24b");
                else
                    areaCheckDescription.setStyle("-fx-background-color: #d29f30");
                userThread.refresh();
            }
        });

        Label sliderDescription = new Label("0 m                                          1000 m                                     2000 m");
        sliderDescription.setStyle("-fx-background-color: #6cf13b");

        areaStuff.getChildren().add(areaCheckDescription);
        areaStuff.getChildren().add(areaSlider);
        areaStuff.getChildren().add(sliderDescription);
        areaStuff.getChildren().add(areaInformation);


        EventHandler<? super MouseEvent> mapMouseButtonHandler = (EventHandler<MouseEvent>) event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                Point2D graphicPoint = new Point2D(event.getX(), event.getY());
                Point mapPoint = mapView.screenToLocation(graphicPoint);

                String coordinatesString = CoordinateFormatter.toLatitudeLongitude(mapPoint, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 6);
                String[] splitCoordinates = coordinatesString.split(" ");
                areaCheckPointY = Double.parseDouble(splitCoordinates[0].replace("N", ""));
                areaCheckPointX = Double.parseDouble((splitCoordinates[1].replace("E", "")));

                if (areaCheckDescription.getStyle().equals("-fx-background-color: #30d24b"))
                    userThread.refresh();
            }
        };
        mapView.setOnMouseClicked(mapMouseButtonHandler);

        areaInformation.setStyle("-fx-background-color: #ef0808");
        areaInformation.setVisible(false);
    }

    @Override
    public void start(Stage stage){

        Thread thread = new Thread(userThread, "T1");

        stage.setTitle("Mpk Wroclaw");
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                userThread.refresh();
                userThread.stop();
            }
        });

        Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        String yourApiKey = "AAPK252690f93bdf4a97873eabb3a8ab7265IOKa3PonrswUJwppgQfTIKQ-6z96Q7jzyw5iuVZk2sSosqKtmmZb2UGfarSrBkrT";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        mapView = new MapView();
        stackPane.getChildren().add(mapView);
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION);
        mapView.setMap(map);
        mapView.setViewpoint(new Viewpoint(51.11114, 17.036694, 90000));

        thread.start();

        addBussesButtons();

        addIntervalButtons();

        addAreaButtons();
    }

    public class MyRunnable implements Runnable {

        private volatile boolean exit = false;
        boolean keepWaiting = true;

        public void stop() {
            exit = true;
        }

        public void refresh() {
            keepWaiting = false;
        }

        @Override

        public void run() {

            while (!exit) {
                GraphicsOverlay myGraphics = new GraphicsOverlay();
                ArrayList<Point> myBusesPointsArray = new ArrayList<>();
                GetBusDoublePosition arrayDonorSystem = new GetBusDoublePosition();

                mapView.getGraphicsOverlays().add(myGraphics);

                String longStringBusInput = null;
                try {
                    longStringBusInput = GetMpkData.getMpkData(vehicleToShowList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if ((!(Objects.equals(longStringBusInput, "[]"))) && (!Objects.equals(longStringBusInput, ""))) {

                    for (String vehicleNumber : vehicleToShowList) {
                        arrayDonorSystem.setPos(longStringBusInput, vehicleNumber, 'x');
                        arrayDonorSystem.setPos(longStringBusInput, vehicleNumber, 'q');

                        int busSize = arrayDonorSystem.getBusXPositions().size();
                        myBusesPointsArray.clear();

                        for (int i = 0; i < busSize; i++) {
                            myBusesPointsArray.add(new Point(arrayDonorSystem.getBusYPositions().get(i), arrayDonorSystem.getBusXPositions().get(i), SpatialReferences.getWgs84()));
                        }
                        busName = arrayDonorSystem.getBusnames();

                        myGraphics.getGraphics().clear();

                        boolean isBusInArea = false;
                        for (int i = 0; i < myBusesPointsArray.size(); i++) {

                            int color;
                            if (!isTram(busName.get(i)))
                                color = 0xFFFF0000;
                            else
                                color = 0xFF0000FF;

                            SimpleMarkerSymbol marker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, color, 12);

                            myGraphics.getGraphics().add(new Graphic(myBusesPointsArray.get(i), marker));
                            TextSymbol markerNr = new TextSymbol();
                            markerNr.setText(busName.get(i));
                            if (isTram(busName.get(i)))
                                markerNr.setColor(0xFFFFFFFF);
                            markerNr.setSize(10);
                            myGraphics.getGraphics().add(new Graphic(myBusesPointsArray.get(i), markerNr));

                            if (doAreaCheck) {

                                PointCollection points = new PointCollection(SpatialReferences.getWgs84());

                                for (int j = 0; j < 31; j++) {
                                    double x_onCircle = areaCheckPointX + (area * 0.01 * 1.533 * Math.cos(j * ((2 * Math.PI) / 30)));
                                    double y_onCircle = areaCheckPointY + (area * 0.01 * 1.533 * 0.62 * Math.sin(j * ((2 * Math.PI) / 30)));
                                    points.add(new Point(x_onCircle, y_onCircle));
                                }


                                Polygon surveillanceArea = new Polygon(points);

                                SimpleLineSymbol lineAroundArea = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF0000, 2);
                                SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x10F0F000, lineAroundArea);

                                myGraphics.getGraphics().add(new Graphic(surveillanceArea, polygonSymbol));

                                SimpleMarkerSymbol areaMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 7);
                                Point areaMarkerPoint = new Point(areaCheckPointX, areaCheckPointY, SpatialReferences.getWgs84());
                                myGraphics.getGraphics().add(new Graphic(areaMarkerPoint, areaMarker));

                                if (distanceBetweenPositions(myBusesPointsArray.get(i).getX(), myBusesPointsArray.get(i).getY(), areaCheckPointX, areaCheckPointY) <= area) {
                                    isBusInArea = true;

                                }
                            }
                        }
                        areaInformation.setVisible(isBusInArea);
                    }
                }
                keepWaiting = true;
                for (int i = 0; i < 50; i++)
                    if (keepWaiting) {
                        try {
                            Thread.sleep(sleepTime / 50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                myGraphics.getGraphics().clear();
            }
        }
    }

    @Override
    public void stop() {

        if (mapView != null) {
            mapView.dispose();
        }
    }


}
