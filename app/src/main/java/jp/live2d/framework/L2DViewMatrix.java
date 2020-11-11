/*
   You can modify and use this source freely
   only for the development of application related Live2D.

   (c) Live2D Inc. All rights reserved.
*/
package jp.live2d.framework;

/*
 * 画面の操作による行列を扱う。
 * 通过操作屏幕来处理矩阵。
 *
 */
public final class L2DViewMatrix extends L2DMatrix44 {
    private float max;
    private float min;

    private float screenLeft; // デバイスに対応する論理座標上の範囲
    private float screenRight;
    private float screenTop;
    private float screenBottom;
    private float maxLeft; // 論理座標上の移動可能範囲（ここからははみ出さない）
    private float maxRight;
    private float maxTop;
    private float maxBottom;

    public L2DViewMatrix() {
        max = 1f;
        min = 1f;
    }

    public final float getMaxScale() {
        return max;
    }

    public final float getMinScale() {
        return min;
    }

    public final boolean isMaxScale() {
        return getScaleX() == max;
    }

    public final void setMaxScale(float v) {
        max = v;
    }

    public final boolean isMinScale() {
        return getScaleX() == min;
    }

    public final void setMinScale(float v) {
        min = v;
    }

    /*
     * 移動量の計算。
     * 移動後の座標が最大範囲をはみ出さないようにする
     * @param shiftX
     * @param shiftY
     */
    public final void adjustTranslate(float shiftX, float shiftY) {
        if (tr[0] * maxLeft + (tr[12] + shiftX) > screenLeft)
            shiftX = screenLeft - tr[0] * maxLeft - tr[12];
        if (tr[0] * maxRight + (tr[12] + shiftX) < screenRight)
            shiftX = screenRight - tr[0] * maxRight - tr[12];
        if (tr[5] * maxTop + (tr[13] + shiftY) < screenTop)
            shiftY = screenTop - tr[5] * maxTop - tr[13];
        if (tr[5] * maxBottom + (tr[13] + shiftY) > screenBottom)
            shiftY = screenBottom - tr[5] * maxBottom - tr[13];

        float tr1[] = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, shiftX, shiftY, 0, 1};
        mul(tr1, tr, tr);
    }

    /*
     * 拡大率の計算。
     * 設定された範囲をはみ出さないようにする。
     */
    public final void adjustScale(float cx, float cy, float scale) {
        float targetScale = scale * tr[0];
        if (targetScale < min) {
            if (tr[0] > 0)
                scale = min / tr[0];
        } else if (targetScale > max) {
            if (tr[0] > 0)
                scale = max / tr[0];
        }

        float tr1[] = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, cx, cy, 0, 1};
        float tr2[] = {scale, 0, 0, 0, 0, scale, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
        float tr3[] = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -cx, -cy, 0, 1};

        mul(tr3, tr, tr);
        mul(tr2, tr, tr);
        mul(tr1, tr, tr);
    }

    /*
     * デバイスに対応する論理座標上の範囲の設定  在与设备相对应的逻辑坐标上设置范围
     * @param left
     * @param right
     * @param bottom
     * @param top
     */
    public final void setScreenRect(float left, float right, float bottom, float top) {
        screenLeft = left;
        screenRight = right;
        screenTop = top;
        screenBottom = bottom;
    }

    /*
     * 論理座標上の移動可能範囲の設定   在逻辑坐标上设置可移动范围
     * @param left
     * @param right
     * @param bottom
     * @param top
     */
    public final void setMaxScreenRect(float left, float right, float bottom, float top) {
        maxLeft = left;
        maxRight = right;
        maxTop = top;
        maxBottom = bottom;
    }

    public final float getScreenLeft() {
        return screenLeft;
    }

    public final float getScreenRight() {
        return screenRight;
    }

    public final float getScreenBottom() {
        return screenBottom;
    }

    public final float getScreenTop() {
        return screenTop;
    }

    public final float getMaxLeft() {
        return maxLeft;
    }

    public final float getMaxRight() {
        return maxRight;
    }

    public final float getMaxBottom() {
        return maxBottom;
    }

    public final float getMaxTop() {
        return maxTop;
    }
}