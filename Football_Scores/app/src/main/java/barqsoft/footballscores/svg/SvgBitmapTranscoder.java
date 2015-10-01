package barqsoft.footballscores.svg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.caverock.androidsvg.SVG;

/**
 * Created by carlosjimenez on 10/1/15.
 */
public class SvgBitmapTranscoder implements ResourceTranscoder<SVG, Bitmap> {
    @Override
    public Resource<Bitmap> transcode(Resource<SVG> toTranscode) {
        SVG svg = toTranscode.get();
        Picture picture = svg.renderToPicture();
        PictureDrawable drawable = new PictureDrawable(picture);

        Bitmap bitmap = Bitmap.createBitmap((int) svg.getDocumentWidth(), (int) svg.getDocumentHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        svg.renderToCanvas(canvas);
        //canvas.drawPicture(resource.getPicture());

        return new SimpleResource<Bitmap>(bitmap);
    }

    @Override
    public String getId() {
        return "";
    }
}


