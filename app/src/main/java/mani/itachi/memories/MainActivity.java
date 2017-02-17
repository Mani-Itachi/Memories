package mani.itachi.memories;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.CommonDataSource;

import mani.itachi.memories.database.MemoryDto;
import mani.itachi.memories.fragments.AboutUsFragment;
import mani.itachi.memories.fragments.CreateMemoryFragment;
import mani.itachi.memories.fragments.HomeFragment;
import mani.itachi.memories.fragments.MemoryDetailFragment;
import mani.itachi.memories.fragments.MyMemoriesFragment;
import mani.itachi.memories.uicomponents.CommonAdapter;
import mani.itachi.memories.utils.Utils;

import static mani.itachi.memories.R.layout.fragment_home;
import static mani.itachi.memories.R.layout.item;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,CommonAdapter.OnGetViewListener {

    public static final int CAM_CODE = 234;
    public static final int READ_WRITE = 235;
    public static final int READ_WRITE_2 = 236;

    Fragment currentFragment;
    Toolbar toolbar;
    AppBarLayout mAppBarLayout;
    Uri cameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermissions();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        currentFragment = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeFrameLayout, currentFragment);
        fragmentTransaction.commitAllowingStateLoss();
        supportInvalidateOptionsMenu();
    }

    public void askPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAM_CODE);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)   {
        if (currentFragment instanceof HomeFragment) {
            getMenuInflater().inflate(R.menu.main, menu);
        }else if (currentFragment instanceof MemoryDetailFragment) {
            getMenuInflater().inflate(R.menu.memory_card_detail, menu);
            MenuItem item = menu.findItem(R.id.action_mycard_edit);
            if (((MemoryDetailFragment) currentFragment).getdialogCreator() != null) {
                if (((MemoryDetailFragment) currentFragment).getEditing()) {
                    item.setIcon(R.drawable.save);
                } else {
                    item.setIcon(R.drawable.edit);
                }
            }
        }else if(currentFragment instanceof CreateMemoryFragment){
            getMenuInflater().inflate(R.menu.create_memory,menu);
        }else{
            getMenuInflater().inflate(R.menu.defaultmenu,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share_myPokemon) {
            HomeFragment homeFragment = (HomeFragment) currentFragment;
            homeFragment.shareMyMemory();
        } else if (id == R.id.action_save_myPokemon) {
            HomeFragment mainFragment = (HomeFragment) currentFragment;
            mainFragment.saveMyMemory();
        } else if (id == R.id.action_mycard_share) {
            MemoryDetailFragment memoryDetailFragment = (MemoryDetailFragment) currentFragment;
            memoryDetailFragment.shareThismemory();
        } else if (id == R.id.action_mycard_edit) {
            MemoryDetailFragment myCardDetailFragment = (MemoryDetailFragment) currentFragment;
            if (myCardDetailFragment.getEditing()) {
                myCardDetailFragment.saveAndToggle();
            } else {
                myCardDetailFragment.toggleShouldEdit();
                item.setIcon(R.drawable.save);
            }
        } else if (id == R.id.action_mycard_delete) {
            MemoryDetailFragment myCardDetailFragment = (MemoryDetailFragment) currentFragment;
            myCardDetailFragment.deleteCard();
        } else if (id == R.id.action_mycard_gallery) {
            MemoryDetailFragment myCardDetailFragment = (MemoryDetailFragment) currentFragment;
            myCardDetailFragment.saveInGallery();
        } else if (id == R.id.action_editfrag_save){

            checkAndSaveCard(currentFragment);

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAM_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                }
                break;
            }
            case READ_WRITE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_2);
                }
                break;
            }
            case READ_WRITE_2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            System.exit(1);
                        }
                    };
                    Toast.makeText(this, "External Storage permission used for saving images denied." +
                            "Please allow this for the app to function properly.Byee..", Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessageDelayed(0, 3000);
                }
                break;
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            if(!(currentFragment instanceof HomeFragment)){
                setFragment(new HomeFragment());
            }

        } else if (id == R.id.nav_mymemories) {

            if(!(currentFragment instanceof MyMemoriesFragment)){
                setFragment(new MyMemoriesFragment());
            }

        } else if (id == R.id.nav_creatememory) {

            if(!(currentFragment instanceof CreateMemoryFragment)){
                CreateMemoryFragment createMemoryFragment = new CreateMemoryFragment();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String currentDate = sdf.format(new Date());
                MemoryDto memoryDto = new MemoryDto(100,"Name Your Memory","type",currentDate,"Why so Special?","pikachu");
                Bundle bundle = new Bundle();
                bundle.putSerializable("MemoryDto",memoryDto );
                Log.v("Helllo","mnai");
                createMemoryFragment.setArguments(bundle);
                setFragment(createMemoryFragment);
            }

        } else if (id == R.id.nav_aboutus) {

            if(!(currentFragment instanceof AboutUsFragment)){
                setFragment(new AboutUsFragment());
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public void setFragment(Fragment fragment) {
        if (currentFragment instanceof CreateMemoryFragment) {
            checkAndSaveCard(fragment);
        } else if (currentFragment instanceof MemoryDetailFragment) {
            MemoryDetailFragment memoryDetailFragment = (MemoryDetailFragment) currentFragment;
            if (memoryDetailFragment.getEditing()) {
                ((MemoryDetailFragment) currentFragment).saveAndToggleAndChange(fragment);
            } else {
                doFragChange(fragment, 0);
            }
        } else {
            doFragChange(fragment, 0);
        }
    }

    private void checkAndSaveCard(final Fragment fragment) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to save the card?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((CreateMemoryFragment) currentFragment).saveMyCard(fragment);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doFragChange(new HomeFragment(), 0);
            }
        });
        builder.show();
    }

    public void startMemImagePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select source");
        final CommonAdapter<String> commonAdapter = new CommonAdapter<>(this);
        List<String> list = new ArrayList<>();
        list.add("Camera");
        list.add("Gallery");
        commonAdapter.setList(list);
        builder.setAdapter(commonAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if ("Camera".equals(commonAdapter.getItem(i))) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, CAM_CODE);
                    } else {
                        startCamera();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 22);
                }
            }
        });

        builder.show();
    }

    public void startCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        cameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, 23);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 22 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4, 3)
                    .setFixAspectRatio(true)
                    .setOutputCompressQuality(30)
                    .start(this);
        }
        if (requestCode == 23 && resultCode == RESULT_OK) {
            CropImage.activity(cameraUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4, 3)
                    .setFixAspectRatio(true)
                    .setOutputCompressQuality(30)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                    bitmap = Bitmap.createScaledBitmap(bitmap, 900, 700, false);
                    if (currentFragment instanceof CreateMemoryFragment) {
                        ((CreateMemoryFragment) currentFragment).setMemoryImage(Utils.getRoundedCornerBitmap(bitmap));
                    } else {
                        ((MemoryDetailFragment) currentFragment).setmemoryImage(Utils.getRoundedCornerBitmap(bitmap));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentFragment instanceof MemoryDetailFragment) {
                setFragment(new MyMemoriesFragment());
            } else if (!(currentFragment instanceof HomeFragment)) {
                setFragment(new HomeFragment());
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public View getView(View convertView, Object item, int position) {
        MyDialogViewHolder myDialogViewHolder;
        if (convertView == null) {
            myDialogViewHolder = new MyDialogViewHolder();
            convertView = LayoutInflater.from(this).inflate(R.layout.activity_imagepicker, null);
            myDialogViewHolder.mTextView = (TextView) convertView.findViewById(R.id.dialogListText);
            myDialogViewHolder.mImageView = (ImageView) convertView.findViewById(R.id.dialogListImage);
            convertView.setTag(myDialogViewHolder);
        } else {
            myDialogViewHolder = (MyDialogViewHolder) convertView.getTag();
        }
        if (item.equals("Camera")) {
            myDialogViewHolder.mTextView.setText("Camera");
            myDialogViewHolder.mImageView.setImageDrawable(getResources().getDrawable(R.drawable.camera));
        } else {
            myDialogViewHolder.mTextView.setText("Gallery");
            myDialogViewHolder.mImageView.setImageDrawable(getResources().getDrawable(R.drawable.gallery));
        }
        return convertView;
    }

    private class MyDialogViewHolder {
        TextView mTextView;
        ImageView mImageView;
    }

    public void shareImage(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tmp" + System.currentTimeMillis(), ".jpg", this.getExternalCacheDir());
            // write the bytes in file
            FileOutputStream fo = new FileOutputStream(tempFile);
            fo.write(bytes.toByteArray());
            fo.close();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(tempFile.getAbsolutePath()));
            startActivity(Intent.createChooser(share, "Share Memory"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean doFragChange(Fragment fragment, int anim) {
        currentFragment = fragment;
        if (currentFragment instanceof HomeFragment) {
            mAppBarLayout = (AppBarLayout) findViewById(R.id.appbarlayout);
            mAppBarLayout.setExpanded(true);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (anim == 0) {
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        fragmentTransaction.replace(R.id.homeFrameLayout, fragment);
        fragmentTransaction.commitAllowingStateLoss();
        supportInvalidateOptionsMenu();
        return true;
    }

}
