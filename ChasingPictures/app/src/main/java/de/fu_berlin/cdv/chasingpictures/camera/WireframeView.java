package de.fu_berlin.cdv.chasingpictures.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * @author Simon
 */
public class WireframeView extends View {
    private final Bitmap wireframe;
    private final Matrix wireframeMatrix;

    public WireframeView(Context context, Bitmap wireframe) {
        super(context);

        this.wireframe = wireframe;

        RectF src = new RectF(0, 0, wireframe.getWidth(), wireframe.getHeight());

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        RectF dst = new RectF(0, 0, metrics.widthPixels, metrics.heightPixels);

        wireframeMatrix = new Matrix();
        wireframeMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(wireframe, wireframeMatrix, null);
    }
}
