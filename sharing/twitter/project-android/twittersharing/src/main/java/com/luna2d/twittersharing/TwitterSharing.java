//-----------------------------------------------------------------------------
// luna2d sharing service for Twitter
// This is part of luna2d engine
// Copyright 2014-2017 Stepan Prokofjev
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
//-----------------------------------------------------------------------------

package com.luna2d.twittersharing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.util.List;
import com.stepanp.luna2d.services.api.*;

public class TwitterSharing extends LunaSharingService
{
    @Override
    public String getName()
    {
		return "twitter";
    }

    @Override
    public void text(String text)
    {

    }

    @Override
    public void image(String imagePath, String text)
    {
        try
        {
            Activity activity = LunaServicesApi.getSharedActivity();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imagePath)));

            List<ResolveInfo> sharingProviders = activity.getPackageManager().queryIntentActivities(intent, 0);

            for(ResolveInfo info : sharingProviders)
            {
                if(info.activityInfo.packageName.equalsIgnoreCase("com.twitter.android"))
                {
                    intent.setPackage(info.activityInfo.packageName);
                    break;
                }
            }

            activity.startActivity(Intent.createChooser(intent, null));
        }
        catch(Exception e)
        {
            Log.e(LunaServicesApi.getLogTag(), "Exception while sharing twitter image " + e.getMessage());
        }

    }
}
