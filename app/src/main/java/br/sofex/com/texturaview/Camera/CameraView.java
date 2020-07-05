package br.sofex.com.texturaview.Camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import br.sofex.com.texturaview.R;
import br.sofex.com.texturaview.databinding.ActivityCameraTextureViewBinding;
import pub.devrel.easypermissions.BuildConfig;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraView extends AppCompatActivity {

    TextureView CameraFotografarTexture;
    TextView CF_Preview_Data;
    CameraDevice cameraDevice;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Size imageDimension;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private String cameraId;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    ImageReader imageReader;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int READ_REQUEST_CODE = 200;
    static final int REQUEST_IMGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,0);
        ORIENTATIONS.append(Surface.ROTATION_90,90);
        ORIENTATIONS.append(Surface.ROTATION_180,180);
        ORIENTATIONS.append(Surface.ROTATION_270,270);
    }
    String filePath; Uri uriFoto;
    Bitmap bitmap2; File file;
    int i = 0; private AlertDialog alerta;
    String mCurrentPhotoPath;
    Integer CountResumedTimesCamera = 0;
    Context mContext;
    Class  classDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        CF_Preview_Data = findViewById(R.id.CF_Preview_Data);
        CameraFotografarTexture = findViewById(R.id.CameraFotografarTexture);
        CameraFotografarTexture.setSurfaceTextureListener(mSurfaceTexture);

        FloatingActionButton fab = findViewById(R.id.FAB_CameraFotografarTexture);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ Fotografar();}
        });

        
    }

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //Toast.makeText(CameraView.this, "TextureView is avaible", Toast.LENGTH_SHORT).show();
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Toast.makeText(CameraView.this, "TextureView is Changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //This is called when the camera is open
            Log.e("App1", "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    //Prepara o Textureview
    TextureView.SurfaceTextureListener mSurfaceTexture = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            /*
             *  O Surface é criado quando a tela é carregada
             */
            //Toast.makeText(CameraFotografar.this, " Surface Criada : \nLargura : "+width +"\nAltura: "+ height, Toast.LENGTH_SHORT).show();
            //openCamera();

            SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(System.currentTimeMillis());
            String DataHoje = formatter.format(date);

            CF_Preview_Data.setText(DataHoje);
            //openBackCamera();
            openCamera();
            CountResumedTimesCamera++;
            if(CameraIdDefault(CameraFotografarTexture.getWidth(),CameraFotografarTexture.getHeight()) == 0){
                //Toast.makeText(CameraFotografar.this, " Camera de Frente ", Toast.LENGTH_SHORT).show();
            }else {
                //Toast.makeText(CameraFotografar.this, " Camera de trás ", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            /*
             *   Quando o buffers size é mudado este trecho é chamado.
             */
            //Toast.makeText(CameraFotografar.this, " Surface tamanho alterado", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            /*
             *  O Surface é destruido quando  o usuário volta para activity anterior,
             *  quando o aplicativo é fechado(óbvio)
             */
            //Toast.makeText(CameraFotografar.this, " Surface Destuido", Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            /*
             *  O Surface é atualizado quando  o usuário maximiza/minimiza o aplicativo e retorna ao aplicativo,
             */
        }
    };

    // Abre a câmera
    private void openCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e("App", "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // Add permission for camera and let user grant the permission
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraFotografar.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }*/
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                manager.openCamera(cameraId, cameraStateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e("app", "openCamera X");
    }
    // Abre a câmera de trás
    private void openBackCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e("App", "is camera open");
        try {

            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[1];

            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraView.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera("1", cameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e("app", "openCamera X");
    }
    // Fecha a câmera
    private void closeCamera(){
        if(cameraDevice != null){
            cameraDevice.close(); //fecha a camera
            cameraDevice = null; // variavel fica nula
        }
    }
    // Verifica o estado da câmera ,se está aberta, fechada ou se deu erro
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //Toast.makeText(CameraFotografar.this, " Camera Aberta ", Toast.LENGTH_SHORT).show();
            cameraDevice = camera;
            createCameraPreview();
            //createCameraPreviewBack();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            //Toast.makeText(CameraFotografar.this, " Camera Fechada ", Toast.LENGTH_SHORT).show();
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
            Toast.makeText(CameraView.this, "Erro :"+error, Toast.LENGTH_SHORT).show();
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e("App1", "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Toast.makeText(CameraFotografar.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    // Camera de frente 0 // Camera de trás 1
    private int CameraIdDefault(int width, int height){
        int codCamera = 0;
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for(String cameraID : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraCharacteristics.LENS_FACING_FRONT){
                    codCamera = 0;
                }
                else if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraCharacteristics.LENS_FACING_BACK){
                    codCamera = 1;
                }
            }
        }catch (CameraAccessException cae){
            //mensagem.ErrorMsg("Error: "+cae);
        }
        return codCamera;
    }
    protected void startBackroundThread(){
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackroundThread(){
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Cria o preview da foto
    protected void createCameraPreview() {
        try {
            // Construa uma nova SurfaceTexture para transmitir imagens para uma determinada textura OpenGL.
            SurfaceTexture surfaceTexture = CameraFotografarTexture.getSurfaceTexture();

            //setDefaultBufferSize : Define o tamanho padrão dos buffers de imagem.
            // surfaceTexture.setDefaultBufferSize(CameraFotografarTexture.getWidth(),CameraFotografarTexture.getHeight());
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            Toast.makeText(CameraView.this, "width :"+width+" - height :"+height, Toast.LENGTH_SHORT).show();

            int heightDisply = displayMetrics.heightPixels + getNavigationBarHeight();
            int WidthDisply  = displayMetrics.widthPixels + getNavigationBarHeight();
            surfaceTexture.setDefaultBufferSize(heightDisply,WidthDisply);

            /*
             * Geralmente, um Surface é criado por ou a partir de um consumidor de buffers de imagem
             * (como SurfaceTexture, MediaRecorder ou Allocation) e é entregue a algum tipo de produtor
             * (como OpenGL, MediaPlayer ou CameraDevice) para atrair.
             */
            Surface surface = new Surface(surfaceTexture);

            // Para obter uma instância do construtor, use o método CameraDevice #createCaptureRequest,
            // que inicializa os campos de solicitação em um dos modelos definidos em CameraDevice.
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            /*
             * Adicione uma superfície à lista de destinos para esta solicitação
             * A superfície adicionada deve ser uma das superfícies incluídas na chamada mais recente para CameraDevice #createCaptureSession,
             * quando a solicitação é feita ao dispositivo da câmera.
             */
            captureRequestBuilder.addTarget(surface);

            /*
             *  CameraCaptureSession.StateCallback()
             *  O retorno de chamada para configurar sessões de captura de uma câmera.
             *  Isso é necessário para verificar se a sessão da câmera está configurada e pronta para mostrar uma visualização.
             *
             *  A callback object for receiving updates about the state of a camera capture session.
             * Este método é chamado quando a sessão começa a processar ativamente solicitações de captura.
             *
             * Se as solicitações de captura forem enviadas antes da chamada de onConfigured (CameraCaptureSession),
             * a sessão começará a processar essas solicitações imediatamente após o retorno de chamada e esse método
             * será chamado imediatamente após onConfigured (CameraCaptureSession).
             */
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    /*
                     * Esse método é chamado quando a fila de captura de entrada do dispositivo da câmera
                     * fica vazia e está pronta para aceitar a próxima solicitação.
                     */
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    /*
                     * Este método é chamado quando a sessão é fechada.
                     */
                    //mensagem.ErrorMsg("Não é possivel carregar o preview da camera");
                }
            },null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Cria o preview da foto
    protected void createCameraPreviewBack() {
        try {
            // Construa uma nova SurfaceTexture para transmitir imagens para uma determinada textura OpenGL.
            SurfaceTexture surfaceTexture = CameraFotografarTexture.getSurfaceTexture();

            //setDefaultBufferSize : Define o tamanho padrão dos buffers de imagem.
            surfaceTexture.setDefaultBufferSize((int)(CameraFotografarTexture.getWidth()/1.5),(int)(CameraFotografarTexture.getHeight()/1.5));

            /*
             * Geralmente, um Surface é criado por ou a partir de um consumidor de buffers de imagem
             * (como SurfaceTexture, MediaRecorder ou Allocation) e é entregue a algum tipo de produtor
             * (como OpenGL, MediaPlayer ou CameraDevice) para atrair.
             */
            Surface surface = new Surface(surfaceTexture);

            // Para obter uma instância do construtor, use o método CameraDevice #createCaptureRequest,
            // que inicializa os campos de solicitação em um dos modelos definidos em CameraDevice.
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            /*
             * Adicione uma superfície à lista de destinos para esta solicitação
             * A superfície adicionada deve ser uma das superfícies incluídas na chamada mais recente para CameraDevice #createCaptureSession,
             * quando a solicitação é feita ao dispositivo da câmera.
             */
            captureRequestBuilder.addTarget(surface);

            /*
             *  CameraCaptureSession.StateCallback()
             *  O retorno de chamada para configurar sessões de captura de uma câmera.
             *  Isso é necessário para verificar se a sessão da câmera está configurada e pronta para mostrar uma visualização.
             *
             *  A callback object for receiving updates about the state of a camera capture session.
             * Este método é chamado quando a sessão começa a processar ativamente solicitações de captura.
             *
             * Se as solicitações de captura forem enviadas antes da chamada de onConfigured (CameraCaptureSession),
             * a sessão começará a processar essas solicitações imediatamente após o retorno de chamada e esse método
             * será chamado imediatamente após onConfigured (CameraCaptureSession).
             */
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    /*
                     * Esse método é chamado quando a fila de captura de entrada do dispositivo da câmera
                     * fica vazia e está pronta para aceitar a próxima solicitação.
                     */
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    /*
                     * Este método é chamado quando a sessão é fechada.
                     */
                    //mensagem.ErrorMsg("Não é possivel carregar o preview da camera");
                }
            },null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    //Atualiza o preview
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e("App", "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            // setRepeatingRequest:  Solicite a captura de imagens repetidamente interminável nesta sessão de captura.
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {e.printStackTrace();
        } catch (NullPointerException null_e){
            //mensagem.ErrorMsgRedirect("Fatal Error \n(CreateCameraPreview(Preview Class)) \n\n"+null_e,CameraFotografar.this, MainActivity.class);
        }
    }
    protected void Fotografar(){
        if(cameraDevice == null){
            Toast.makeText(this, "Camera Nula", Toast.LENGTH_SHORT).show();
        }else{
            try {i++;
                ImageReader reader = ImageReader.newInstance(CameraFotografarTexture.getWidth(), CameraFotografarTexture.getHeight(), ImageFormat.JPEG, 1);
                List<Surface> outputSurfaces = new ArrayList<Surface>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(CameraFotografarTexture.getSurfaceTexture()));
                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                String timeStamp = new SimpleDateFormat(" dd_MM_yyyy").format(new Date());
                String pictureFile = "Imagem_Perfil_" + i +"_"+ timeStamp +".jpg";
                file = new File("/data/data/br.sofex.com.facialmap/cache/"+pictureFile);

                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                       // Mensagem mensagem = new Mensagem(CameraFotografar.this);
                        // image to bitmap
                        Image image = null;
                        try {
                            image = imageReader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            save(bytes,file);
                        } catch (FileNotFoundException e) {
                            //e.printStackTrace();
                            Log.e("App", "Error :"+e);
                        } catch (IOException e) {
                            //e.printStackTrace();
                            Log.e("App", "Error :"+e);
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                        SaveImagePreview(file);
                    }
                    private void save(byte[] bytes,File file) throws IOException {
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(file);
                            output.write(bytes);
                        } finally {
                            if (null != output) {
                                output.close();
                            }
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        //Toast.makeText(CameraFotografar.this, "Saved:"+file, Toast.LENGTH_LONG).show();
                        createCameraPreview();
                    }
                };
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                    }
                }, mBackgroundHandler);

            }catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        startBackroundThread();
        if(CountResumedTimesCamera > 1 )
        {openBackCamera();}
        Toast.makeText(this, "Camera Resumida", Toast.LENGTH_SHORT).show();
        if(CameraFotografarTexture.isAvailable()){
            openCamera();
            //openBackCamera();
        }else{
            CameraFotografarTexture.setSurfaceTextureListener(mSurfaceTexture);
        }
    }
    @Override
    protected void onPause(){
        stopBackroundThread();
        closeCamera();
        Toast.makeText(this, "Camera Fechada", Toast.LENGTH_SHORT).show();
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        closeCamera();
        Toast.makeText(this, "Camera Fechada", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // now, you have permission go ahead
            //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivityForResult(intent, REQUEST_TAKE_PHOTO);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(CameraView.this,
                    Manifest.permission.READ_CALL_LOG)) {
                EasyPermissions.requestPermissions(CameraView.this, "Este aplicativo precisa acessar a câmera para prosseguir . Por favor autorize o acesso a câmera.", READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                // now, user has denied permission permanently!
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Voçê negou o acesso ao aplicativo.\n" +
                        "Você precissa aprovar a(s) permissão(ôes)", Snackbar.LENGTH_LONG).setAction("Alterar", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                    }
                });
                snackbar.show();
            }

        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    public void CheckImage( File file){

        Bitmap bitmapFinal = null;

        uriFoto = Uri.fromFile(file);
        Bitmap bitmap1 = BitmapFactory.decodeFile(file.getAbsolutePath());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG,100,bos);
        byte[] bitmapdata = bos.toByteArray();

        //Funciona 100%
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.toString());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            bitmapFinal = rotateBitmap(bitmap1, orientation);
        } catch (IOException e) {
            Log.e("App"," Error "+e);
            e.printStackTrace();
        }

        //String path1 = Environment.getExternalStorageDirectory()+"/Android/data/br.sofex.com.imageexifinterface/files/Pictures/"+"Converted.jpg";
        String path1 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/"+"Converted.jpg";//storage/emulated/0/Android/data/br.sofex.com.mapearfoto/files/Pictures
        saveBitmap(bitmapFinal,path1); // criar um arquivo da foto corrigida de bitmap
        //Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        //mensagem.MsgImg(bitmapFinal);
    }
    private File saveBitmap(Bitmap bitmap, String path) {
        File file = null;
        if (bitmap != null) {
            file = new File(path);
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path); //here is set your file path where you want to save or also here you can set file object directly

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // bitmap is your Bitmap instance, if you want to compress it you can compress reduce percentage
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }
    private void SaveImagePreview(final File fileimage) {

        final String[] FilePath = new String[1];
        final String[] FilePathNome = new String[1];
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //LayoutInflater é utilizado para inflar nosso layout em uma view.
        //-pegamos nossa instancia da classe
        LayoutInflater li = getLayoutInflater();

        //inflamos o layout alerta.xml na view
        View view = li.inflate(R.layout.show_foto_perfil, null);
        //definimos para o botão do layout um clickListener
        final ImageView img1 = view.findViewById(R.id.show_imgPerfilUser);

        final TextView DataPreviewDialog = view.findViewById(R.id.DataDialogPerfil);
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(System.currentTimeMillis());
        String DataHoje = formatter.format(date);
        DataPreviewDialog.setText(DataHoje);

        //TODO: Rotaciona o arquivo de foto  para a orientação correta, e passa para o bitmap
        final Bitmap bit = FixOrientatioImage(Uri.fromFile(new File(fileimage.getAbsolutePath())));
        //Bitmap bit = BitmapFactory.decodeFile(fileimage.getAbsolutePath());

        //TODO: Salva o arquivo corrigido para pasta /storage/self/primary/Android/data/br.sofex.com.mapdb/files/Pictures
        saveBitmap(bit,fileimage.getAbsolutePath());
        //Bitmap bit1 = BitmapFactory.decodeFile(fileimage.getAbsolutePath());

        img1.setImageBitmap(bit);
        Button BtnPreviewSave = view.findViewById(R.id.BtnPreviewFotoPerfilSave);
        BtnPreviewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Copia o arquivo corrigido para pasta /data/data/br.sofex.com.mapdb/cache
                try {
                    File from = fileimage;
                    File to = new File("/data/data/br.sofex.com.facialmap/cache/","Preview.jpg");
                    FilePath[0] = to.getAbsolutePath();
                    FilePathNome[0] = to.getName();
                    copyFromTo(from,to);
                } catch (IOException e) {e.printStackTrace();}

                //TODO: Deleta a pasta com a imagem selecionada
                File mFile = new File("/storage/self/primary/Android/data/br.sofex.com.facialmap/" + "files");
                try { deleteFolder(mFile);}
                catch (IOException e)
                {Toast.makeText(CameraView.this, "Unable to delete folder", Toast.LENGTH_SHORT).show();}
                if(mFile.exists()){
                    //mensagem.ErrorMsg("Erro em remover a pasta !");
                    Toast.makeText(CameraView.this, "Erro em remover a pasta !", Toast.LENGTH_SHORT).show();
                }
                else{
                    //mensagem.SucessMsg("Imagem Salva com sucesso !");
                    Toast.makeText(CameraView.this, "Imagem Salva com sucesso !", Toast.LENGTH_SHORT).show();
                }

                alerta.dismiss();
            }
        });
        Button BtnPreviewCancel = view.findViewById(R.id.BtnPreviewFotoPerfilCancel);
        BtnPreviewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File mFile = new File("/storage/self/primary/Android/data/br.sofex.com.facialmap/files");
                Log.e("App1","Path :"+mFile);
                if(mFile.exists()){
                    try {deleteFolder(mFile);alerta.dismiss();}
                    catch (IOException e)
                    {Toast.makeText(CameraView.this, "Unable to delete folder", Toast.LENGTH_SHORT).show();}
                }else{alerta.dismiss();}
            }
        });

        builder.setView(view);
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe
        alerta.show();
        alerta.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
        TextView messageView = alerta.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir();
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        FileOutputStream out = new FileOutputStream(image);
        out.close();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        //Toast.makeText(this, " Path : "+mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
        Log.v("App"," Path1 "+mCurrentPhotoPath);
        return image;
    }
    /*TODO: FUNÇÃO DE RETORNO DE BITMAP PARA PORTRAIT*/
    private Bitmap FixOrientatioImage(final Uri selectedImageUri) {
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

            ExifInterface exif = new ExifInterface(selectedImageUri.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("App","integer :"+orientation);
            int angle = 0;

            switch (orientation) {

                case ExifInterface.ORIENTATION_NORMAL:
                    Log.e("App1","integer :"+orientation);
                    angle = 270;
                    Log.e("App1","ORIENTATION_NORMAL angle "+angle);
                    Toast.makeText(CameraView.this, "ORIENTATION_NORMAL angle "+angle, Toast.LENGTH_SHORT).show();
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.e("App2","integer :"+orientation);
                    angle = 90;
                    Log.e("App1","ORIENTATION_ROTATE_90 angle "+angle);
                    Toast.makeText(CameraView.this, "ORIENTATION_ROTATE_90 angle "+angle, Toast.LENGTH_SHORT).show();
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.e("App3","integer :"+orientation);
                    angle = 180;
                    Log.e("App1","ORIENTATION_ROTATE_180 angle "+angle);
                    Toast.makeText(CameraView.this, "ORIENTATION_ROTATE_180 angle "+angle, Toast.LENGTH_SHORT).show();
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.e("App4","integer :"+orientation);
                    angle = 270;
                    Log.e("App1","ORIENTATION_ROTATE_270 angle "+angle);
                    Toast.makeText(CameraView.this, "ORIENTATION_ROTATE_270 angle "+angle, Toast.LENGTH_SHORT).show();
                    break;

                default:
                    angle = 0;
                    Log.e("App1","default angle "+angle);
                    Toast.makeText(CameraView.this, "ORIENTATION_ROTATE_270 angle "+angle, Toast.LENGTH_SHORT).show();
                    break;
            }

            Matrix mat = new Matrix();

            if (angle == 0 && bm.getWidth() > bm.getHeight()){
                Toast.makeText(CameraView.this, "angle "+angle, Toast.LENGTH_SHORT).show();
                mat.postRotate(270);}
            else
                mat.postRotate(angle);

            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, true);

        }
        catch (IOException e) {Log.e("", "-- Error in setting image"); }
        catch (OutOfMemoryError oom) {
            Log.e("", "-- OOM Error in setting image");
        }
        return null;
    }
    /*TODO: FUNÇÃO PARA COPIAR ARQUIVOS */
    public static void copyFromTo(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
    public void deleteFolder(File folder) throws IOException {
        if (folder.isDirectory()) {
            for (File ct : folder.listFiles()){
                deleteFolder(ct);
            }
        }
        if (!folder.delete()) {
            throw new FileNotFoundException("Unable to delete: " + folder);
        }
    }


    public boolean showNavigationBar(Resources resources)
    {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }
    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else return 0;
        }
        return 0;
    }


}
