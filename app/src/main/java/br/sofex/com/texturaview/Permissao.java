package br.sofex.com.texturaview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import pub.devrel.easypermissions.BuildConfig;
import pub.devrel.easypermissions.EasyPermissions;

public class Permissao implements  ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * permissions request code
     */
    private static final int READ_REQUEST_CODE = 200;
    static final int REQUEST_IMGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int CAM_REQUEST = 3;
    private final int CODE = 12;

    Context mContext;
    Activity activity;
    public Permissao(Context context)
    {this.mContext = context; activity = (Activity) mContext;}

    public boolean checkpermissao()
    {
        //check if app has permission to access the external storage.
        if (EasyPermissions.hasPermissions(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //TODO: Chama e Função
           return  true;
        } else {
            //If permission is not present request for the same.
            EasyPermissions.requestPermissions(activity,"Este aplicativo precisa acessar certos recursos :\n1 - Câmera\n2 - Armazenamento interno.\n\nPor favor autorize o acesso.\nEste aplicativo necessita da câmera e do armazenamento interno.",READ_REQUEST_CODE,Manifest.permission.READ_EXTERNAL_STORAGE);
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) ){
                // not permanently denied
            }
            else {
                    Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "Voçê negou o acesso ao aplicativo.\n" +
                     "Você precissa aprovar a(s) permissão(ôes)", Snackbar.LENGTH_LONG).setAction("Alterar", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                    }
                });
                snackbar.show();
            }
            return  false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // now, you have permission go ahead
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activity.startActivityForResult(intent, REQUEST_TAKE_PHOTO);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.READ_CALL_LOG)) {
                EasyPermissions.requestPermissions(activity, "Este aplicativo precisa acessar certos recursos :\n1 - Câmera\n2 - Armazenamento interno.\n\nPor favor autorize o acesso.\nEste aplicativo necessita da câmera e do armazenamento interno.",
                READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                  // now, user has denied permission permanently!
                  Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "Voçê negou o acesso ao aplicativo.\n" +
                  "Você precissa aprovar a(s) permissão(ôes)", Snackbar.LENGTH_LONG).setAction("Alterar", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                    }
                });
                snackbar.show();
            }

        }
    }



}
