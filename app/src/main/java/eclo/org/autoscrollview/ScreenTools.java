package eclo.org.autoscrollview;

import android.content.Context;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: 伯约
 * Date: 15/5/13
 * Time: 上午10:35
 * ScreenTools
 * To change this template use File | Settings | File Templates.
 */
public class ScreenTools
{
    private Context mCtx;
    private static ScreenTools mScreenTools;

    public static ScreenTools instance(Context ctx) {
        if(null == mScreenTools) {
            mScreenTools = new ScreenTools(ctx);
        }

        return mScreenTools;
    }

    private ScreenTools(Context ctx) {
        this.mCtx = ctx.getApplicationContext();
    }

    public int getScreenWidth() {
        return this.mCtx.getResources().getDisplayMetrics().widthPixels;
    }

    public int dip2px(int dip) {
        float density = this.getDensity(this.mCtx);
        return (int)((double)((float)dip * density) + 0.5D);
    }

    public int dip2px(float dip) {
        float density = this.getDensity(this.mCtx);
        return (int)((double)(dip * density) + 0.5D);
    }

    public int px2dip(int px) {
        float density = this.getDensity(this.mCtx);
        return (int)(((double)px - 0.5D) / (double)density);
    }

    public int px2dip(float px) {
        float density = this.getDensity(this.mCtx);
        return (int)(((double)px - 0.5D) / (double)density);
    }

    public int getScreenDensityDpi() {
        return this.mCtx.getResources().getDisplayMetrics().densityDpi;
    }

    public float getXdpi() {
        return this.mCtx.getResources().getDisplayMetrics().xdpi;
    }

    public float getYdpi() {
        return this.mCtx.getResources().getDisplayMetrics().ydpi;
    }

    public float getDensity(Context ctx) {
        return ctx.getResources().getDisplayMetrics().density;
    }

    public int getScal() {
        return this.getScreenWidth() * 100 / 480;
    }

    public int get480Height(int height480) {
        int width = this.getScreenWidth();
        return height480 * width / 480;
    }

    public int getStatusBarHeight() {
        Class c = null;
        Object obj = null;
        Field field = null;
        boolean x = false;
        int sbar = 0;

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            int x1 = Integer.parseInt(field.get(obj).toString());
            sbar = this.mCtx.getResources().getDimensionPixelSize(x1);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return sbar;
    }

    public int getScreenHeight() {
        return this.mCtx.getResources().getDisplayMetrics().heightPixels;
    }
}
