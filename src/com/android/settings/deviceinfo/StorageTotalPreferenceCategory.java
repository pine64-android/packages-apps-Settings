/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.android.settings.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class StorageTotalPreferenceCategory extends PreferenceCategory {

    private StorageItemPreference mRamTotal;
    private StorageItemPreference mFlashTotal;

    private long mRamTotalSize;
    private long mFlashTotalSize;

    public StorageTotalPreferenceCategory(Context context) {
        super(context);

        setTitle(context.getText(R.string.memory_info));
    }

    private StorageItemPreference buildItem(int titleRes, int colorRes) {
        return new StorageItemPreference(getContext(), titleRes, colorRes);
    }

    public void init() {
        final Context context = getContext();

        removeAll();

        mRamTotal = buildItem(R.string.memory_ram_size, 0);
        mFlashTotal = buildItem(R.string.memory_flash_size, 0);
        //mRamTotal.setSelectable(false);
        //mFlashTotal.setSelectable(false);
        addPreference(mRamTotal);
        addPreference(mFlashTotal);

        mRamTotalSize = getRamSize();
        mFlashTotalSize = getFlashSize();
        mRamTotal.setSummary(formatSize(mRamTotalSize));
        mFlashTotal.setSummary(formatSize(mFlashTotalSize));
    }

    private long getRamSize() {
        long size = android.os.Process.getTotalMemory();
        double pow = Math.ceil(Math.log(size - 1) / Math.log(2));
        size = (long)Math.pow(2, pow);
        return size;
    }

    private long readLineLong(File file) {
        BufferedReader br = null;
        long size = 0;
        try {
            br = new BufferedReader(new FileReader(file));
            String tmp = br.readLine();
            if (!TextUtils.isEmpty(tmp)) {
                size = Long.parseLong(tmp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return size;
    }

    private long getFlashSize() {
        File blockDir = new File("/sys/block");
        File list[] = blockDir.listFiles();
        long totalNand = 0;
        long totalEmmc = 0;
        for (File blockFile: list) {
            if (blockFile.getName().contains("nand")) {
                File sizeFile = new File(blockFile, "size");
                totalNand += readLineLong(sizeFile);
            } else if (blockFile.getName().equals("mmcblk0")) {
                File sizeFile = new File(blockFile, "size");
                totalEmmc = readLineLong(sizeFile);
                if (totalEmmc > 0)
                    break;
            }
        }
        long size = 0;
        if (totalNand > 0) {
            double pow = Math.ceil(Math.log(totalNand) / Math.log(2));
            size = (long)Math.pow(2, pow + 10 - 1);
        } else if (totalEmmc > 0) {
            double pow = Math.ceil(Math.log(totalEmmc) / Math.log(2));
            size = (long)Math.pow(2, pow + 10 - 1);
        }
        return size;
    }

    private String formatSize(long size) {
        return Formatter.formatFileSize(getContext(), size);
    }
}
