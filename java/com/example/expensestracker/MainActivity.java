package com.example.expensestracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expensestracker.calendar.CalendarDataPass;
import com.example.expensestracker.calendar.CalendarEvent;
import com.example.expensestracker.calendar.CalendarFragment;
import com.example.expensestracker.calendar.DeadlineEvent;
import com.example.expensestracker.calendar.EditEvent;
import com.example.expensestracker.calendar.ExpenseCategory;
import com.example.expensestracker.calendar.ExpenseCategoryCallback;
import com.example.expensestracker.calendar.ExpenseCategoryDAO;
import com.example.expensestracker.calendar.ExpensesEvent;
import com.example.expensestracker.calendar.IncomeEvent;
import com.example.expensestracker.dialogs.AmountConfirmationDialog;
import com.example.expensestracker.dialogs.DeadlineDialog;
import com.example.expensestracker.dialogs.EditDeadlineDialog;
import com.example.expensestracker.dialogs.EditEventDialog;
import com.example.expensestracker.dialogs.EditExpenseDialog;
import com.example.expensestracker.helpers.CalendarHelper;
import com.example.expensestracker.helpers.CameraHelper;
import com.example.expensestracker.helpers.CreateEventFromImage;
import com.example.expensestracker.helpers.ImageText;
import com.example.expensestracker.helpers.ImageTextCallback;
import com.example.expensestracker.monthlyinfo.MonthlyExpense;
import com.example.expensestracker.monthlyinfo.MonthlyExpenseDAO;
import com.example.expensestracker.monthlyinfo.MonthlyInfoEntity;
import com.example.expensestracker.monthlyinfo.MonthlyInfoFragment;
import com.example.expensestracker.notifications.AlarmReceiver;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.example.expensestracker.monthlyinfo.PassMonthlyData;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener, PassMonthlyData, CalendarDataPass, EditEvent, CreateEventFromImage {
    private static final int NOTIFICATION_STATUS_CODE = 1;
    private static final int CAMERA_STATUS_CODE = 2;

    // UI Elements of the main page
    FrameLayout addMonthlyInfoFragment;
    private Button addBtn;
    private Button initializeBtn;
    private Button cameraBtn;
    private Button confirmBtn;
    private ImageButton takePictureBtn;
    private ImageButton cameraBackBtn;
    private FragmentManager manager;
    private TextView overview;
    private TextView dashboardLabel;

    // Unique notification ID for sending notifications
    private int notificationId = 1;

    // Number of deadlines the user has added, obtained from the size of DeadlineEvent ArrayList
    private int deadlineCount = 0;

    // User initialized expenses, income, and budget, which is calculated from subtracting expenses from income
    private double income = 0;
    private double expenses = 0;
    private double budget = 0;

    // Total net gain from all calendar events for both expense and income
    private double netExpenseFromCalendar = 0;
    private double netIncomeFromCalendar = 0;

    // The additional expense/income gain from calendar events if the user last added any new events
    private double additionalExpensesFromCalendar = 0;
    private double additionalIncomeFromCalendar = 0;

    // Boolean variables to keep track of the current fragment that is active, if any. This is used for the onBackPressed listener.
    private boolean monthlyInfoFragmentActive, calendarEventFragmentActive;

    // Data structs for storing all user added events in the current month
    // Nested Hashmap<LocalDate, ArrayList<CalendarEvent>> to support multiple events on a single day
    private HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> monthlyMapping;

    // Simple ArrayList<DeadlineEvent> which stores all user added deadlines, regardless of month
    private ArrayList<DeadlineEvent> deadlines;

    private List<MonthlyExpense> monthlyExpenses = new ArrayList<MonthlyExpense>();

    // HashMap data structure that holds the specific id and Intent object for each deadline, will be used for cancelling alarms
    // The id (key) is the integer value passed into the 'requestCode' argument of PendingIntent, and Intent (value) is the intent object used in the PendingIntent
    private HashMap<Integer, Intent> deadlineIntentMapping = new HashMap<Integer, Intent>();

    private boolean upToDate = true;
    private boolean notificationsEnabled = true;
    private ExpensesTrackerDatabase db;
    private AlarmManager alarmManager;

    // References to child fragments/dialogs of MainActivity
    // Allows us to call methods such as updating UI and syncing data from the MainActivity
    private CalendarFragment calendar;
    private DeadlineDialog deadlineDialog;
    private MonthlyInfoFragment monthlyInfo;

    // Data members for interacting with Camera2 API
    private TextureView textureView;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader reader;
    private ImageView annotatedImage;
    private String imageText = null;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private int rotation;

    // Extracted total from receipt
    private double total;


    // Callback for handling camera device actions
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // This callback happens when cameraManager calls its open method
            hideMainUI();
            cameraDevice = camera;
            takePictureBtn.setVisibility(View.VISIBLE);
            cameraBackBtn.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.VISIBLE);
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i("Camera disconnect", "Closing camera...");
            camera.close();
            unhideMainUI();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            // When the surface texture for textureView is ready (this essentially happens at startup)
            Log.i("Surface Texture", "Surface texture now available");
            cameraBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_STATUS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationsEnabled = true;
            } else {
                notificationsEnabled = false;
            }
        } else if (requestCode == CAMERA_STATUS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Camera permission granted, press the button again to start camera", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Camera permissions were denied. Enable camera permissions to use this feature", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Initializing all UI elements and obtaining a reference to them
        addBtn = findViewById(R.id.addBtn);
        initializeBtn = findViewById(R.id.initializeBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        takePictureBtn = findViewById(R.id.takePictureBtn);
        cameraBackBtn = findViewById(R.id.cameraBackBtn);
        confirmBtn = findViewById(R.id.confirmBtn);

        overview = findViewById(R.id.overviewText);
        dashboardLabel = findViewById(R.id.dashboardLabel);
        addMonthlyInfoFragment = findViewById(R.id.monthlyInfoFragment);
        textureView = findViewById(R.id.textureView);
        annotatedImage = findViewById(R.id.imageView);

        // Setting some views to be initially invisible
        cameraBtn.setVisibility(View.INVISIBLE);
        takePictureBtn.setVisibility(View.INVISIBLE);
        cameraBackBtn.setVisibility(View.INVISIBLE);
        annotatedImage.setVisibility(View.INVISIBLE);
        confirmBtn.setVisibility(View.INVISIBLE);

        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        textureView.setSurfaceTextureListener(textureListener);

        manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Log.i("Current Month", String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));

        // Setting on click listeners for the add and initialize expenses buttons
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = new CalendarFragment();
                hideMainUI();
                calendarEventFragmentActive = true;
                monthlyInfoFragmentActive = false;
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.calendarFragment, calendar);
                transaction.addToBackStack("Calendar");
                transaction.commit();
            }
        });
        initializeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthlyInfo = new MonthlyInfoFragment(expenses, income, monthlyExpenses);
                hideMainUI();
                monthlyInfoFragmentActive = true;
                calendarEventFragmentActive = false;
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(addMonthlyInfoFragment.getId(), monthlyInfo);
                transaction.addToBackStack("Monthly Information");
                transaction.commit();
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button the user clicks to turn on the camera
                if (CameraHelper.checkCameraHardware(MainActivity.this)) {
                    CameraManager cameraManager = (CameraManager) MainActivity.this.getSystemService(Context.CAMERA_SERVICE);
                    String rearCameraId = CameraHelper.getRearCameraId(MainActivity.this, cameraManager);
                    if (rearCameraId != null) {
                        // Check for camera permissions. If permission is not granted, request it
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                // Turn on the camera with specified id. In this case, it is the default, rear-facing camera
                                // Also assign a CameraDevice.StateCallback to detect when the camera is successfully opened
                                cameraManager.openCamera(rearCameraId, stateCallback, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, CAMERA_STATUS_CODE);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No rear camera detected", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Device does not have a camera", Toast.LENGTH_LONG).show();
                }
            }
        });
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        cameraBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitCamera();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAmount(total);
            }
        });
        // Requesting notification permissions if permissions are not allowed
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_STATUS_CODE);
            return;
        }
        FirebaseApp.initializeApp(this);
        initializeDatabase();
    }

    public void exitCamera() {
        cameraDevice.close();
        cameraDevice = null;
        textureView.setVisibility(View.INVISIBLE);
        takePictureBtn.setVisibility(View.INVISIBLE);
        cameraBackBtn.setVisibility(View.INVISIBLE);
        annotatedImage.setVisibility(View.INVISIBLE);
        confirmBtn.setVisibility(View.INVISIBLE);
        unhideMainUI();
    }

    public void createCameraPreview() {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        try {
            if (texture == null) {
                Log.i("Surface texture", "Couldn't obtain surface texture");
                Toast.makeText(MainActivity.this, "Couldn't obtain surface texture", Toast.LENGTH_LONG).show();
                return;
            }

            texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            List<Surface> outputSurface = new ArrayList<Surface>(1);
            outputSurface.add(surface);
            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) {
                        Log.i("Camera device", "Camera device is null");
                        return;
                    }
                    captureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void updatePreview() {
        if (cameraDevice == null) {
            Log.i("Update preview", "Update preview error");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            // This line is responsible for "refreshing" the camera preview constantly, otherwise the camera preview in the TextureView will only display one frame
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewAPI")
    public void takePicture() {
        if (cameraDevice == null) {
            Log.i("Camera Device", "Camera is null");
            return;
        }
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size imageSize[] = null;
            if (characteristics != null) {
                imageSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (imageSize != null && imageSize.length > 0) {
                width = imageSize[0].getWidth();
                height = imageSize[0].getHeight();
            }
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            ArrayList<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            CaptureRequest.Builder captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequest.addTarget(reader.getSurface());
            captureRequest.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            Log.i("Take Picture", "Successfully took picture");

            rotation = getWindowManager().getDefaultDisplay().getRotation();
            Log.i("Rotation", "Rotation=" + rotation);
            int rotationCompensation = ORIENTATIONS.get(rotation);
            int sensorOrientation = cameraManager.getCameraCharacteristics(cameraDevice.getId()).get(CameraCharacteristics.SENSOR_ORIENTATION);

            // Back-facing camera rotation calculation
            rotation = (sensorOrientation - rotationCompensation + 360) % 360;

            ImageReader.OnImageAvailableListener imageListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // When an image is available after the user takes a picture
                    Log.i("Image avaiable", "Obtained image, processing...");
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte bytes[] = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes.clone(), 0, bytes.length, null);

                        // Obtain an InputImage object from the existing Image object, which will will be fed to the ML Kit OCR to extract text from the image
                        InputImage mediaImage = InputImage.fromMediaImage(image, 0);
                        extractImageText(mediaImage, bitmap, new ImageTextCallback() {
                            @Override
                            public double onImageTextAvailable(String text) {
                                imageText = text;

                                // Obtain a list of all doubles present within the receipt image using regex
                                List<Double> receiptValues = ImageText.processImageText(imageText);

                                // Go through the list and find the max value, which represents the total amount of the receipt
                                double total_amount = ImageText.get_total(receiptValues, ImageText.shouldTakeSecondMax(imageText));
                                Log.i("Receipt Total", "Total=" + total_amount);
                                return total_amount;
                            }
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(imageListener, backgroundHandler);

            CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureRequest.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    reader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1);
                    takePicture();
                    createCameraPreview();
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void save(byte[] bytes) throws IOException {
        File imageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Images");
        File imageFile = null;
        if (!imageDirectory.exists()) {
            if (imageDirectory.mkdirs()) {
                Toast.makeText(this, "Created images folder. Image can be found in the 'Images' folder", Toast.LENGTH_SHORT).show();
                imageFile = new File(imageDirectory, "receipt_img.jpg");
            } else {
                Toast.makeText(this, "Couldn't create 'Images' folder, saving image to root instead", Toast.LENGTH_SHORT).show();
                imageFile = new File(getExternalFilesDir(null), "receipt_img.jpg");
            }
        } else {
            imageFile = new File(imageDirectory, "receipt_img.jpg");
        }
        FileOutputStream outputStream = new FileOutputStream(imageFile);


        try {
            outputStream.write(bytes);
            Toast.makeText(this, "Successfully saved image", Toast.LENGTH_SHORT).show();
        } finally {
            outputStream.close();
        }
    }

    // Image is fed through optical character recognition (OCR) from ML Kit to obtain a String value from an image
    public void extractImageText(InputImage mediaImage, Bitmap bitmap, ImageTextCallback callback) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(mediaImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                // Pass the resulting text produced by OCR to ImageTextCallback, returns the total amount that was extracted from the receipt image
                total = callback.onImageTextAvailable(text.getText());
                if (total < 0) {
                    Toast.makeText(MainActivity.this, "Couldn't recognize receipt image, please try again!", Toast.LENGTH_LONG).show();
                    exitCamera();
                    return;
                }

                List<Text.TextBlock> textBlocks = text.getTextBlocks();
                Bitmap annotatedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(annotatedBitmap);
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(15);
                boolean foundTotal = false;

                // Loop through all TextBlocks, and within each TextBlock, loop through all TextLines
                // to find a line of text that matches the amount
                for (int i = 0; i < textBlocks.size(); i++) {
                    List<Text.Line> textLines = textBlocks.get(i).getLines();
                    for (int j = 0; j < textLines.size(); j++) {
                        if (textLines.get(j).getText().contains(Double.toString(total))) {
                            // Matching TextLine has been found, draw a rectangle around the line
                            Rect r = textLines.get(j).getBoundingBox();
                            canvas.drawRect(r, paint);

                            // Matching line was found, we don't need to loop through any further, so break
                            foundTotal = true;
                            break;
                        }
                    }
                    if (foundTotal) {
                        break;
                    }
                }

                // On a separate UI thread, change visibility of views to display the image that was captured by the user
                // along with the resulting rectangle that was drawn
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        annotatedImage.setImageBitmap(annotatedBitmap);
                        annotatedImage.setRotation(90);
                        annotatedImage.setVisibility(View.VISIBLE);
                        confirmBtn.setVisibility(View.VISIBLE);
                        textureView.setVisibility(View.INVISIBLE);
                        takePictureBtn.setVisibility(View.INVISIBLE);

                        Toast.makeText(MainActivity.this, "Text extraction successful, please confirm the receipt total amount", Toast.LENGTH_LONG).show();
                    }
                });
                Log.i("IMAGE TEXT", "Text=" + text.getText());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { e.printStackTrace(); }
        });
    }

    public void confirmAmount(double amount) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<ExpenseCategory> currentCategories;
                if (calendar != null) {
                    currentCategories = calendar.getCategories();
                } else {
                    ExpenseCategoryDAO expenseCategoryDAO = db.expenseCategoryDAO();
                    currentCategories = expenseCategoryDAO.getAllCategories();
                }
                AmountConfirmationDialog confirmationDialog = new AmountConfirmationDialog(amount, currentCategories);
                confirmationDialog.show(getSupportFragmentManager(), "CONFIRMATION");
            }
        });
    }

    @Override
    @SuppressLint("NewAPI")
    public void createEvent(double amount, ExpenseCategory selectedCategory) {
        ExpensesEvent newEvent = new ExpensesEvent(amount, 0, LocalDate.now());
        newEvent.setCategory(selectedCategory);
        CalendarHelper.insertEvent(monthlyMapping, newEvent, MainActivity.this);
        sumEventsInCurrentMonth();
        overview.setText(String.join("",generateUpdatedText(expenses, income, budget, deadlineCount)));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                LocalDate currentDate = LocalDate.now();
                CalendarEventsDAO dao = db.calendarEventsDAO();
                ExpenseCategoryDAO expenseCategoryDAO = db.expenseCategoryDAO();

                int category_id;
                List<ExpenseCategory> isExistingCategory = expenseCategoryDAO.getCategory(selectedCategory.getName());
                if (isExistingCategory == null || isExistingCategory.isEmpty()) {
                    expenseCategoryDAO.createCategory(selectedCategory);
                }
                category_id = expenseCategoryDAO.getCategoryId(selectedCategory.getName());
                CalendarEventsEntity entity = new CalendarEventsEntity();
                entity.month = currentDate.getMonthValue();
                entity.day = currentDate.getDayOfMonth();
                entity.year = currentDate.getYear();
                entity.expense = amount;
                entity.income = 0;
                entity.category_id = category_id;
                dao.insert(entity);
            }
        });
        exitCamera();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (monthlyInfoFragmentActive) {
            monthlyInfoFragmentActive = false;
            double expenses = monthlyInfo.getExpensesInput();
            double income = monthlyInfo.getIncomeInput();
            if (expenses >= 0 && income >= 0) {
                monthlyInfo.getMonthlyDataPasser().onDataPassed(monthlyInfo.getExpensesInput(), monthlyInfo.getIncomeInput());
            }
            getSupportFragmentManager().popBackStack();
            unhideMainUI();
        } else if (calendarEventFragmentActive) {
            calendarEventFragmentActive = false;
            unhideMainUI();
            sumEventsInCurrentMonth();
            getSupportFragmentManager().popBackStack();
        }
    }

    public ExpensesTrackerDatabase getDatabase() { return db; }

    public void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(), ExpensesTrackerDatabase.class, "ExpensesTracker").build();
        Log.i("Database creation", "Success!");

        // CountDownLatch for synchronization to ensure that retrieving deadlines from the database is completed first, before checking for pending DeadlineEvents in deletePassedDeadlines
        CountDownLatch secondLatch = new CountDownLatch(3);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                retrieveMonthlyInfoFromDatabase(secondLatch);
                retrieveMonthlyExpensesFromDatabase();
                retrieveCalendarEventsFromDatabase(secondLatch);
                retrieveDeadlinesFromDatabase(secondLatch);
            }
        });
        try {
            secondLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        deletePassedDeadlines();
    }

    public void setDeadlineDialog(DeadlineDialog dialog) {
        deadlineDialog = dialog;
    }

    // Retrieves list of MonthlyExpenses from the database
    public void retrieveMonthlyExpensesFromDatabase() {
        MonthlyExpenseDAO dao = db.monthlyExpenseDAO();
        monthlyExpenses = dao.getMonthlyExpenses();
        Log.i("MONTHLY INFO", "# of expenses=" + monthlyExpenses.size());
    }

    // Grabs monthly expense and income value from previous state
    public void retrieveMonthlyInfoFromDatabase(CountDownLatch latch) {
        MonthlyInfoDAO monthlyInfoDAO = new MonthlyInfoDAO_Impl(db);
        List<MonthlyInfoEntity> monthlyInfo = monthlyInfoDAO.getMonthlyInfo();
        if (monthlyInfo != null && !monthlyInfo.isEmpty()) {
            Log.i("Monthly Info", "Fecthing saved data: Expenses = " + monthlyInfo.get(0).expenses + " Income = " + monthlyInfo.get(0).income);
            income = monthlyInfo.get(0).income;
            expenses = monthlyInfo.get(0).expenses;
            double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
        } else {
            Log.i("Monthly Info", "No valid info to fetch!");
        }
        latch.countDown();
    }
    // Grabs all deadline events from previous state
    @SuppressLint("NewAPI")
    public void retrieveDeadlinesFromDatabase(CountDownLatch latch) {
        DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
        List<DeadlineEventsEntity> deadlinesInDatabase = dao.getDeadlineEvents();
        if (deadlinesInDatabase != null && !deadlinesInDatabase.isEmpty()) {
            Log.i("Deadlines", "Fetching deadlines...");
            deadlines = new ArrayList<DeadlineEvent>();
            for (int i = 0; i < deadlinesInDatabase.size(); i++) {
                int day = deadlinesInDatabase.get(i).day;
                int month = deadlinesInDatabase.get(i).month;
                int year = deadlinesInDatabase.get(i).year;
                int id = deadlinesInDatabase.get(i).key;
                int hour = deadlinesInDatabase.get(i).hour;
                int hour_type = deadlinesInDatabase.get(i).hour_type;
                int minute = deadlinesInDatabase.get(i).minute;
                LocalDate date = LocalDate.of(year, month, day);
                double expense = deadlinesInDatabase.get(i).expense;
                String information = deadlinesInDatabase.get(i).information;
                DeadlineEvent deadline = new DeadlineEvent(expense, 0, date, information);
                deadline.setHour(hour);
                deadline.setAmOrPm(hour_type);
                deadline.setMinute(minute);
                deadlines.add(deadline);

                // Reconstruct all of the Intent objects associated with the PendingIntents for each alarm from the previous state
                // We then add add the key-value pair to the Intent mapping, with the queried id as the key, and Intent object as the value
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.setAction("com.example.expensestracker.ACTION_TRIGGER_ALARM");
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                intent.putExtra("information", information);
                intent.putExtra("amount", expense);
                deadlineIntentMapping.put(id, intent);
            }
            deadlineCount = deadlines.size();
            //overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlineCount)));
        } else {
            Log.i("Deadlines", "Could not fetch deadlines");
        }
        latch.countDown();
    }

    // Grabs all calendar events from previous state, and initializes local HashMap data struct with the data
    @SuppressLint("NewAPI")
    public void retrieveCalendarEventsFromDatabase(CountDownLatch latch) {
        CalendarEventsDAO dao = new CalendarEventsDAO_Impl(db);
        List<CalendarEventsEntity> calendarEvents = dao.getCalendarEvents();
        if (calendarEvents != null) {
            monthlyMapping = new HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>>();
            for (int i = 0; i < calendarEvents.size(); i++) {
                int year = calendarEvents.get(i).year;
                int month = calendarEvents.get(i).month;
                int day = calendarEvents.get(i).day;
                int category_id = calendarEvents.get(i).category_id;
                double expenses = calendarEvents.get(i).expense;
                double income = calendarEvents.get(i).income;
                LocalDate date = LocalDate.of(year, month, day);
                netExpenseFromCalendar += expenses;
                netIncomeFromCalendar += income;

                // Retrieved calendar event is of instance ExpenseEvent
                if (income == 0) {
                    ExpensesEvent event = new ExpensesEvent(expenses, income, date);
                    ExpenseCategoryDAO expenseCategoryDAO = db.expenseCategoryDAO();
                    ExpenseCategory expenseCategory = expenseCategoryDAO.getCategory(category_id).get(0);
                    event.setCategory(expenseCategory);
                    CalendarHelper.insertEvent(monthlyMapping, event, MainActivity.this);
                // Retrieved calendar event is of instance IncomeEvent
                } else {
                    IncomeEvent event = new IncomeEvent(expenses, income, date);
                    CalendarHelper.insertEvent(monthlyMapping, event, MainActivity.this);
                }
            }
            additionalExpensesFromCalendar = netExpenseFromCalendar - additionalExpensesFromCalendar;
            additionalIncomeFromCalendar = netIncomeFromCalendar - additionalIncomeFromCalendar;
            budget += Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            if (deadlines != null && !deadlines.isEmpty()) {
                overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlines.size())));
            } else {
                overview.setText(String.join("", generateUpdatedText(expenses, income, budget, 0)));
            }
        }
        latch.countDown();
    }

    @SuppressLint("NewApi")
    // Method to check for any DeadlineEvents pending deletion (this happens when the deadline alarm has gone off)
    // Delete the DeadlineEvent from the database upon app startup
    public void deletePassedDeadlines() {
        if (deadlines != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("deadline_to_remove", Context.MODE_PRIVATE);

            // Get the number of deadlines that have already passed
            int numberOfDeadlines = sharedPreferences.getInt("number_of_deadlines", 0);

            // Loop through the data stored in SharedPreferences, which contains passed deadlines' date, information, and amount
            for (int i = 0; i < numberOfDeadlines; i++) {
                int day = sharedPreferences.getInt("day" + (i + 1), -1);
                int month = sharedPreferences.getInt("month" + (i + 1), -1);
                int year = sharedPreferences.getInt("year" + (i + 1), -1);
                String information = sharedPreferences.getString("information" + (i + 1), "");
                double amount = Double.longBitsToDouble(sharedPreferences.getLong("amount" + (i + 1), Double.doubleToLongBits(-1)));

                if (day < 0 || month < 0 || year < 0 || information.equals("") || amount < 0) {
                    Log.i("Pending Deadlines", "Could not retrieve info for deadline pending deletion");
                    continue;
                }

                // Delete the passed DeadlineEvent from the local database
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                        dao.deleteDeadlineEvent(amount, information, month, day, year);
                    }
                });

                int index = deadlines.indexOf(new DeadlineEvent(amount, 0, LocalDate.of(year, month, day), information));
                if (index >= 0) {
                    deadlines.remove(index);
                }
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            overview.setText(String.join("", generateUpdatedText(expenses, income, budget, deadlines.size())));
        } else {
            overview.setText(String.join("", generateUpdatedText(expenses, income, budget, 0)));
            Log.i("Deadlines", "No deadlines were set");
        }
    }

    // Helper function to synchronize the ArrayList of deadlines across 3 class instances
    // Syncs deadlines between MainActivity, CalendarFragment, and DeadlineDialog
    // Necessary because otherwise, changes made in MainActivity's ArrayList won't be reflected in the other classes
    public void synchronizeDeadlines() {
        if (calendar != null) {
            calendar.setDeadlines(deadlines);
        }
        if (deadlineDialog != null) {
            deadlineDialog.setDeadlines(deadlines);
            deadlineDialog.initializeDeadlinesView();
        }
    }

    @SuppressLint("NewAPI")
    // Method to set an alarm for a deadline. Automatically called by CalendarFragment after successfully adding a DeadlineEvent
    public void setAlarmForDeadline(DeadlineEvent deadline) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Create the intent object, and insert the necessary extras into it
                Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
                alarmIntent.setAction("com.example.expensestracker.ACTION_TRIGGER_ALARM");
                alarmIntent.putExtra("year", deadline.getYear());
                alarmIntent.putExtra("month", deadline.getMonth());
                alarmIntent.putExtra("day", deadline.getDay());
                alarmIntent.putExtra("information", deadline.getInformation());
                alarmIntent.putExtra("amount", deadline.getExpenses());

                // Query the database, and obtain the id for the corresponding deadline
                DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                int id = dao.getUUID(deadline.getAmount(), deadline.getInformation(), deadline.getMonth(), deadline.getDay(), deadline.getYear());
                Log.i("Deadline ID", "ID=" + id);

                // Use the id we obtained to insert a key-value pair of the id (as the key) and intent object (as the value)
                deadlineIntentMapping.put(id, alarmIntent);

                // Pass id as an argument into PendingIntent. Since the id is autogenerated in the database, this ensures a unique PendingIntent for each DeadlineEvent
                // This allows us to easily remove an alarm for a deadline if the user chooses to remove a specific DeadlineEvent
                PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar endDate = Calendar.getInstance();
                endDate.set(deadline.getYear(), deadline.getMonth() - 1, deadline.getDay());
                if (deadline.getAmOrPm() == CalendarEvent.AM) {
                    if (deadline.getHour() == 12) {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour() + 12);
                    } else {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour());
                    }
                } else {
                    if (deadline.getHour() == 12) {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour());
                    } else {
                        endDate.set(Calendar.HOUR_OF_DAY, deadline.getHour() + 12);
                    }
                }
                endDate.set(Calendar.MINUTE, deadline.getMinute());

                long triggerTime = endDate.getTimeInMillis() - System.currentTimeMillis();
                Log.i("Trigger time", "Trigger time=" + triggerTime);

                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + triggerTime, pIntent);
            }
        });

    }

    @Override
    // This is called everytime the user returns to the main page.
    // We want to update the information every time the main page is returned to.
    public void onBackStackChanged() {
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));
    }


    @Override
    // Abstract interface method onDataPassed is called when returning from the MonthlyInfoFragment (popBackStack() is called)
    // MonthlyInfoFragment is where the user initially sets their monthly expenses and income
    public void onDataPassed(double expenses, double income) {
        setMonthlyInfo(expenses + additionalExpensesFromCalendar, income + additionalIncomeFromCalendar);
        overview.setText(String.join("", generateUpdatedText(this.expenses, this.income, this.budget, deadlineCount)));

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Updating monthly_info table in the database with the new information
                // monthly_info has only one row at any given time, with two columns representing the monthly expense and monthly income
                MonthlyInfoDAO monthlyDAO = db.monthlyInfoDAO();
                monthlyDAO.clearMonthlyInfo();
                MonthlyInfoEntity newMonthlyData = new MonthlyInfoEntity();
                newMonthlyData.income = income + additionalIncomeFromCalendar;
                newMonthlyData.expenses = expenses + additionalExpensesFromCalendar;
                monthlyDAO.insert(newMonthlyData);
                Log.i("Monthly Info DAO", "Successfully inserted new data");
            }
        });
    }

    @Override
    public void passMonthlyExpenseList(List<MonthlyExpense> monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    // Implementation of the createExpense method from PassMonthlyData interface; called after the user confirms the creation of a MonthlyExpense
    @Override
    public void createExpense(MonthlyExpense expense) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MonthlyExpenseDAO dao = db.monthlyExpenseDAO();
                dao.insert(expense);
                Log.i("MONTHLY EXPENSE", "Successfully inserted MonthlyExpense into database");
            }
        });
    }

    // Called when the user clicks on a MonthlyExpense within the RecyclerView
    // Displays a dialog fragment with information specific to the selected MonthlyExpense
    @Override
    public void openExpenseDialog(MonthlyExpense expense) {
        Log.i("MONTHLY EXPENSE", "DESCRIPTION=" + expense.getDescription());
        Log.i("MONTHLY EXPENSE", "AMOUNT=" + expense.getAmount());
        int index = monthlyExpenses.indexOf(expense);
        if (index < 0) {
            Log.i("MONTHLY EXPENSE", "Expense does not exist");
            return;
        }
        EditExpenseDialog dialog = new EditExpenseDialog(expense, index);
        dialog.show(getSupportFragmentManager(), "EDIT_EXPENSE");
    }

    // Called when user edits a MonthlyExpense after changing either the description or amount
    @Override
    public void updateExpense(String newDescription, double newAmount, MonthlyExpense previousExpense, int index) {
        String previousDescription = previousExpense.getDescription();
        double previousAmount = previousExpense.getAmount();

        // Replace the old MonthlyExpense with a new MonthlyExpense object with the updated information
        monthlyExpenses.set(index, new MonthlyExpense(newDescription, newAmount));

        // Reinitialize MonthlyInfoFragment's list of MonthlyExpenses with the updated list containing the new object
        if (monthlyInfo != null) {
            monthlyInfo.setMonthlyExpense(monthlyExpenses);
            monthlyInfo.updateExpenseAmount(newAmount - previousAmount);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MonthlyExpenseDAO dao = db.monthlyExpenseDAO();
                dao.updateExpense(newDescription, newAmount, previousDescription, previousAmount);
            }
        });
    }

    // Called when the user clicks confirm on the delete dialog when selecting a MonthlyExpense
    @Override
    public void deleteExpense(int index) {
        MonthlyExpense expenseToDelete = monthlyExpenses.get(index);
        monthlyExpenses.remove(index);

        if (monthlyInfo != null) {
            monthlyInfo.setMonthlyExpense(monthlyExpenses);
            monthlyInfo.updateExpenseAmount(-expenseToDelete.getAmount());
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MonthlyExpenseDAO dao = db.monthlyExpenseDAO();
                dao.deleteExpense(expenseToDelete.getDescription(), expenseToDelete.getAmount());
            }
        });
        Toast.makeText(MainActivity.this, "Successfully deleted monthly expense", Toast.LENGTH_SHORT).show();
    }

    @Override
    // Same idea as onDataPassed, but for CalendarFragment instead
    // This passes back the hashmap that keeps track of events in all of the months back to the main page
    public void onCalendarDataPassed(HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> mapping, ArrayList<DeadlineEvent> deadlines) {
        monthlyMapping = mapping;
        this.deadlines = deadlines;
        if (this.deadlines != null) {
            deadlineCount = this.deadlines.size();
        }
        else {
            Log.i("Failure", "Could not process mapping data");
        }
    }
    @Override
    @SuppressLint("NewAPI")
    // This method is called via a callback from an EditEvent object
    // In this case, an EditEvent object in the CustomAdapter for the RecyclerView in CalendarFragment has called sendCalendarEventDate
    // The EditEvent object calls this method after the user has clicked an element within the RecyclerView list
    public void sendCalendarEventDate(int month, int year, int day) {
        Log.i("Send Date", month + "/" + day + "/" + year);

        // Using the month value, we obtain the hashmap for that specific month from monthlyMapping
        // We obtain the ArrayList of all events on that particular day by using the month, year, and day to query the particular month HashMap by using a LocalDate with those parameters
        HashMap<LocalDate, ArrayList<CalendarEvent>> currentMonthEvents = monthlyMapping.get(month);
        ArrayList<CalendarEvent> events = currentMonthEvents.get(LocalDate.of(year, month, day));

        // Create an EditEventDialog, passing the obtained ArrayList of CalendarEvents to the dialog
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ExpenseCategoryDAO dao = db.expenseCategoryDAO();
                List<ExpenseCategory> categories = dao.getAllCategories();
                EditEventDialog dialog = new EditEventDialog(events, categories);
                dialog.show(getSupportFragmentManager(), "Edit Event");
            }
        });
    }

    @SuppressLint("NewAPI")
    @Override
    // Using the arguments passed from clicking on an element in DeadlineDialog's RecyclerView, make a DeadlineEvent object
    // Compare this object with the rest of the DeadlineEvent objects in the DeadlineEvent ArrayList
    public void sendDeadlineEventDate(double amount, String information, int month, int year, int day, int hour, int minute, String hourType) {
        DeadlineEvent targetDeadline = new DeadlineEvent(amount, 0, LocalDate.of(year, month, day), information);
        targetDeadline.setHour(hour);
        targetDeadline.setMinute(minute);
        if (hourType.equals("AM")) {
            targetDeadline.setAmOrPm(CalendarEvent.AM);
        } else {
            targetDeadline.setAmOrPm(CalendarEvent.PM);
        }
        for (int i = 0; i < deadlines.size(); i++) {
            if (deadlines.get(i).equals(targetDeadline)) {
                // DeadlineEvent object is equal to an object in the ArrayList
                // Proceed with making an EditDeadlineDialog and display this to the user
                // We also pass the DeadlineEvent object within the ArrayList to the constructor of EditDeadlineDialog and the index in the ArrayList
                // The object argument provides a reference so that any changes we make to this object is reflected back in the ArrayList, and the index is for in the case of deleting
                EditDeadlineDialog dialog = new EditDeadlineDialog(i, deadlines);
                dialog.show(getSupportFragmentManager(), "Edit Deadline");
                return;
            }
        }
        Log.i("Not Found", "Couldn't locate deadline in current list");
    }

    @Override
    // Called from EditEventDialog when the user confirms an edit to an existing CalendarEvent after selecting a particular date
    // In MainActivity, we simply update the information in the database, while the CalendarEvent object that was modified was modified in EditEventDialog
    public void modifyCalendarEvent(CalendarEvent targetEvent, double amount) {
        Log.i("New Amount", targetEvent.getMonth() + "/" + targetEvent.getDay() + "/" + targetEvent.getYear() + ": $" + amount);

        // This simply updates the RecyclerView to display the new amount set by the user
        if (calendar != null) {
            calendar.initializeRecyclerView();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Here we utilize a custom query to update the event stored in the database with the new amount
                // Two separate methods, depending on whether the event is of class ExpensesEvent or IncomeEvent
                CalendarEventsDAO dao = db.calendarEventsDAO();
                if (targetEvent instanceof ExpensesEvent) {
                    ExpenseCategoryDAO expenseCategoryDAO = db.expenseCategoryDAO();
                    int new_category_id = expenseCategoryDAO.getCategoryId(((ExpensesEvent) targetEvent).getCategory().getName());
                    dao.updateExpenseEvent(amount, new_category_id, targetEvent.getMonth(), targetEvent.getDay(), targetEvent.getYear());
                } else {
                    dao.updateIncomeEvent(amount, targetEvent.getMonth(), targetEvent.getDay(), targetEvent.getYear());
                }
            }
        });
    }

    @Override
    // Called from EditDeadlineDialog via callback when the user confirms an edit to an existing DeadlineEvent
    // When the user modifies a deadline, we will actually create a new deadline with the updated info, and delete the old deadline alarm
    // This is done because there is no way to update the information in an Intent object after it has been bound to the PendingIntent, so we have to create a new one entirely
    public void modifyDeadlineEvent(DeadlineEvent targetDeadline, String previousInformation, double previousAmount, int index) {
        synchronizeDeadlines();

        // Database operation updating DeadlineEvent information
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // First step is to cancel the old deadline alarm with the outdated information
                // Get the id of the particular DeadlineEvent, use this id to obtain the specific Intent object from the HashMap
                // With the id and Intent object, create the exact PendingIntent object that was used to set the alarm, to cancel the alarm
                DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                cancelDeadlineAlarm(targetDeadline, dao);

                // Database query to update the DeadlineEvent in the database with updated information
                // Call setAlarmForDeadline method to set a new alarm with a new Intent object that contains the updated information as extras
                dao.updateDeadlineEvent(targetDeadline.getAmount(),
                        targetDeadline.getInformation(),
                        targetDeadline.getMonth(),
                        targetDeadline.getDay(),
                        targetDeadline.getYear(),
                        targetDeadline.getHour(),
                        targetDeadline.getMinute(),
                        targetDeadline.getAmOrPm(),
                        previousInformation);
                setAlarmForDeadline(targetDeadline);
                Log.i("Deadline Update", "Success");
            }
        });
    }

    @Override
    @SuppressLint("NewAPI")
    // Called from EditDeadlineDialog when the user deletes an existing CalendarEvent
    public void deleteCalendarEvent(CalendarEvent selectedEvent) {

        // Check and see if after deletion of selected CalendarEvent, there are no remaining events for this day
        // If so, remove this key-value pair from the HashMap
        if (monthlyMapping.containsKey(selectedEvent.getMonth())) {
            HashMap<LocalDate, ArrayList<CalendarEvent>> eventsOnDay = monthlyMapping.get(selectedEvent.getMonth());
            LocalDate targetDate = LocalDate.of(selectedEvent.getYear(), selectedEvent.getMonth(), selectedEvent.getDay());
            if (eventsOnDay.containsKey(targetDate)) {
                ArrayList<CalendarEvent> events = eventsOnDay.get(targetDate);
                if (events.isEmpty()) {
                    eventsOnDay.remove(targetDate);
                }
            }
        }

        // We then update the RecyclerView in CalendarFragment after removing the specified DeadlineEvent from the ArrayList
        if (calendar != null) {
            ExpenseCategoryCallback callback = (ExpenseCategoryCallback) calendar;
            if (selectedEvent instanceof ExpensesEvent) {
                callback.onDeleteEvent(((ExpensesEvent) selectedEvent).getCategory());
            } else {
                callback.onDeleteEvent(new ExpenseCategory("Income"));
            }
            calendar.initializeRecyclerView();
        }

        // Database operation deleting CalendarEvent
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                CalendarEventsDAO dao = new CalendarEventsDAO_Impl(db);
                if (selectedEvent.isExpense()) {
                    dao.deleteExpenseEvent(selectedEvent.getExpenses(), selectedEvent.getDay(), selectedEvent.getMonth(), selectedEvent.getYear());
                } else {
                    dao.deleteIncomeEvent(selectedEvent.getIncome(), selectedEvent.getDay(), selectedEvent.getMonth(), selectedEvent.getYear());
                }
            }
        });
    }

    @Override
    // Called when the user deletes an existing DeadlineEvent
    public void deleteDeadlineEvent(int index, DeadlineEvent targetDeadline) {
        // Database operation deleting DeadlineEvent
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Obtain the id (from the database) associated with the DeadlineEvent to be deleted.
                // This is the same integer value that was used to initially set the alarm for the deadline
                // Also access the HashMap<Integer, Intent> using the given id as key, this gives the exact intent that was associated with the PendingIntent for setting the alarm

                DeadlineEventsDAO dao = new DeadlineEventsDAO_Impl(db);
                cancelDeadlineAlarm(targetDeadline, dao);
                dao.deleteDeadlineEvent(targetDeadline.getAmount(), targetDeadline.getInformation(), targetDeadline.getMonth(), targetDeadline.getDay(), targetDeadline.getYear());
            }
        });

        deadlines.remove(index);
        deadlineCount = deadlines.size();
        synchronizeDeadlines();
    }

    public void cancelDeadlineAlarm(DeadlineEvent deadline, DeadlineEventsDAO dao) {
        int id = dao.getUUID(deadline.getAmount(), deadline.getInformation(), deadline.getMonth(), deadline.getDay(), deadline.getYear());
        if (deadlineIntentMapping.containsKey(id)) {
            Intent intent = deadlineIntentMapping.get(id);
            PendingIntent pIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pIntent);
            Log.i("Delete Deadline", "Canceled alarm with id=" + id);
        } else {
            Log.i("Cancel Deadline", "Couldn't cancel deadline alarm (invalid deadline)");
        }
    }

    public void clearDeadlineAlarms(DeadlineEventsDAO dao) {
        for (Map.Entry<Integer, Intent> element : deadlineIntentMapping.entrySet()) {
            int id = element.getKey();
            Intent intent = element.getValue();
            PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pIntent);
        }
    }

    // We iterate through the hashmap entry for the current month, and find the sum of all of the expenses and income that the user added for the current month
    // Update the information displayed on the main page accordingly
    public void sumEventsInCurrentMonth() {
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            double totalAdditionalMonthlyExpenses = 0;
            double totalAdditionalMonthlyIncome = 0;
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            HashMap<LocalDate, ArrayList<CalendarEvent>> currentMonthEvents = monthlyMapping.get(currentMonth);
            if (currentMonthEvents != null && !currentMonthEvents.isEmpty()) {
                for (Map.Entry<LocalDate, ArrayList<CalendarEvent>> entry : currentMonthEvents.entrySet()) {
                    ArrayList<CalendarEvent> events = entry.getValue();
                    ArrayList<Double> dayExpensesAndIncome = CalendarHelper.calculateTotalBudget(events);
                    for (int i = 0; i < events.size(); i++) {
                        notificationsEnabled = true;
                        if (events.get(i) instanceof ExpensesEvent) {
                            totalAdditionalMonthlyExpenses += events.get(i).getExpenses();
                        } else if (events.get(i) instanceof IncomeEvent) {
                            totalAdditionalMonthlyIncome += events.get(i).getIncome();
                        }
                    }
                }
            }
            // Difference between net and additional:
            // Net is the total additional expense/income from the given month
            // Additional is the amount that was added since the last user-added event
            // Ex. If a user adds 4 events: 2 income, and 2 expense event.
            // The expense event is worth $50 (-50) each, and the income event is worth $100 (+100) each
            // Net expense would be -$100, and net income would +$200.
            // But assume that the user added these events not all at once, but in two separate sessions
            // In the first session, they add 1 expense/income, and the second, they add the remaining 1 expense/income
            // After first session, net expense = additionalMonthlyExpenses = 50, net income = additionalMonthlyIncome = 100
            // After second session, net expense = 100, net income = 200. AdditionalMonthlyExpenses = (net expense) - 50 = 50. AdditionalMonthlyIncome = (net income) - 100 = 100
            // Thus, additional represents the amount gained since the last calendar event update

            additionalExpensesFromCalendar = totalAdditionalMonthlyExpenses - netExpenseFromCalendar;
            additionalIncomeFromCalendar = totalAdditionalMonthlyIncome - netIncomeFromCalendar;
            netExpenseFromCalendar = totalAdditionalMonthlyExpenses;
            netIncomeFromCalendar = totalAdditionalMonthlyIncome;
            budget += Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
            Log.i("New Budget", String.valueOf(budget));
        }
        overview.setText(String.join("",generateUpdatedText(expenses, income, budget, deadlineCount)));
    }


    // Helper method to easily update the main page text with updated information
    public String[] generateUpdatedText(double expenses, double income, double budget, int deadlineCount) {
        String[] res = new String[7];
        res[0] = "Monthly Expenses: $" + expenses;
        res[1] = "\nMonthly Income: $" + income;
        res[2] = "\nAdditional Expenses From Calendar: $" + Math.round(netExpenseFromCalendar * 100.0) / 100.0;
        res[3] = "\nAdditional Income From Calendar: $" + netIncomeFromCalendar;
        res[4] = "\nAdditional Budget From Calendar: $" + Math.round((netIncomeFromCalendar - netExpenseFromCalendar) * 100.0) / 100.0;
        res[5] = "\nTotal Available Budget: $" + budget;
        res[6] = "\n" + deadlineCount + " Upcoming Deadlines";
        return res;
    }

    public void hideMainUI() {
        overview.setVisibility(View.INVISIBLE);
        addBtn.setVisibility(View.INVISIBLE);
        cameraBtn.setVisibility(View.INVISIBLE);
        initializeBtn.setVisibility(View.INVISIBLE);
        dashboardLabel.setVisibility(View.INVISIBLE);
    }

    public void unhideMainUI() {
        overview.setVisibility(View.VISIBLE);
        addBtn.setVisibility(View.VISIBLE);
        cameraBtn.setVisibility(View.VISIBLE);
        initializeBtn.setVisibility(View.VISIBLE);
        dashboardLabel.setVisibility(View.VISIBLE);
    }

    // Helper method to encapsulate updating data members with new values
    public void setMonthlyInfo(double expenses, double income) {
        this.expenses = expenses;
        this.income = income;
        double additionalBudgetFromCalendar = Math.round((additionalIncomeFromCalendar - additionalExpensesFromCalendar) * 100.0) / 100.0;
        this.budget = (Math.round((income - expenses) * 100.0) / 100.0) + additionalBudgetFromCalendar;
    }

    public HashMap<Integer, HashMap<LocalDate, ArrayList<CalendarEvent>>> getMonthlyMapping() { return monthlyMapping; }

    public ArrayList<DeadlineEvent> getDeadlines() {
        return deadlines;
    }

    // Method to clear all data, both monthly and calendar (all months)
    public void resetInfo() {
        if (monthlyMapping != null && !monthlyMapping.isEmpty()) {
            monthlyMapping.clear();
        }
        if (deadlines != null && !deadlines.isEmpty()) {
            deadlines.clear();
        }
        if (monthlyExpenses != null && !monthlyExpenses.isEmpty()) {
            monthlyExpenses.clear();
        }

        // Database operation that wipes information from all 3 tables (monthly_info, calendar_events, deadline_events)
        // Essentially a full reset of all user-set info
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MonthlyInfoDAO monthlyInfoDAO = db.monthlyInfoDAO();
                CalendarEventsDAO calendarDAO = db.calendarEventsDAO();
                DeadlineEventsDAO deadlineDAO = db.deadlineEventsDAO();
                MonthlyExpenseDAO monthlyExpenseDAO = db.monthlyExpenseDAO();

                // Loop through the deadline id and intent HashMap, cancel all alarms set by alarmManager
                clearDeadlineAlarms(deadlineDAO);
                deadlineIntentMapping.clear();

                // Wipe all data from all 4 tables in the Room Database
                monthlyInfoDAO.clearMonthlyInfo();
                calendarDAO.clearCalendarEvents();
                deadlineDAO.clearDeadlineEvents();
                monthlyExpenseDAO.clearMonthlyExpenses();
            }
        });

        income = 0;
        expenses = 0;
        budget = 0;
        deadlineCount = 0;
        additionalExpensesFromCalendar = 0;
        additionalIncomeFromCalendar = 0;
        netExpenseFromCalendar = 0;
        netIncomeFromCalendar = 0;
        Log.i("Main Info", "Expenses: " + expenses + "\nIncome: " + income + "\nBudget: " + budget);
        overview.setText(String.join("", generateUpdatedText(expenses, income, budget, 0)));
        unhideMainUI();
    }
}