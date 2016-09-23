package com.bread.animatortest;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends Activity {

    static final float BALL_SIZE = 50f;
    static final float FULL_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        container.addView(new MyAnimationView(this));
    }

    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {
        public final ArrayList<ShapeHolder> balls = new ArrayList<>();

        public MyAnimationView(Context context) {
            super(context);
            setBackgroundColor(Color.WHITE);
            //背景的渐变
            ObjectAnimator colorAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.color_anim);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setTarget(this);
            colorAnim.start();

        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            this.invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //如果触碰事件不是按下、移动事件
            if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
                return false;
            }
            ShapeHolder newBall = addBall(event.getX(), event.getY());
            //小球下落开始时的y坐标
            float startY = newBall.getY();
            float endY = getHeight() - BALL_SIZE;

            float h = getHeight();
            float eventY = event.getY();
            int duration = (int) (FULL_TIME * ((h - eventY) / h));
            //定义小球下落的动画
            ValueAnimator fallAnim = ObjectAnimator.ofFloat(newBall, "y", startY, endY);
            fallAnim.setDuration(duration);
            //插值方式：加速
            fallAnim.setInterpolator(new AccelerateInterpolator());
            //添加监听器
            //当ValueAnimator的属性发生改变时，将会激发该监听器的事件监听方法
            fallAnim.addUpdateListener(this);


            ValueAnimator squashAnim1 = ObjectAnimator.ofFloat(newBall, "x", newBall.getX(), newBall.getX() - BALL_SIZE / 2);
            squashAnim1.setDuration(duration / 4);
            //重复1数
            squashAnim1.setRepeatCount(1);
            //重复方式：REVERSE 倒退
            squashAnim1.setRepeatMode(ValueAnimator.REVERSE);
            //插值方式：减速
            squashAnim1.setInterpolator(new DecelerateInterpolator());
            squashAnim1.addUpdateListener(this);

            ValueAnimator squashAnim2 = ObjectAnimator.ofFloat(newBall, "width", newBall.getWidth(), newBall.getWidth() + BALL_SIZE);
            squashAnim2.setDuration(duration / 4);
            squashAnim2.setRepeatCount(1);
            squashAnim2.setRepeatMode(ValueAnimator.REVERSE);
            squashAnim2.setInterpolator(new DecelerateInterpolator());
            squashAnim2.addUpdateListener(this);

            ObjectAnimator stretchAnim1 = ObjectAnimator.ofFloat(newBall, "y", endY, endY + BALL_SIZE / 2);
            stretchAnim1.setDuration(duration / 4);
            stretchAnim1.setRepeatCount(1);
            stretchAnim1.setRepeatMode(ValueAnimator.REVERSE);
            stretchAnim1.setInterpolator(new DecelerateInterpolator());
            stretchAnim1.addUpdateListener(this);

            ObjectAnimator stretchAnim2 = ObjectAnimator.ofFloat(newBall, "height", newBall.getHeitht(), newBall.getHeitht() - BALL_SIZE / 2);
            stretchAnim2.setDuration(duration / 4);
            stretchAnim2.setRepeatCount(1);
            stretchAnim2.setRepeatMode(ValueAnimator.REVERSE);
            stretchAnim2.setInterpolator(new DecelerateInterpolator());
            stretchAnim2.addUpdateListener(this);

            ObjectAnimator bounceBackAnim = ObjectAnimator.ofFloat(newBall, "y", endY, startY);
            bounceBackAnim.setDuration(duration);
            bounceBackAnim.setInterpolator(new DecelerateInterpolator());
            bounceBackAnim.addUpdateListener(this);

            AnimatorSet bouncer = new AnimatorSet();
            //在squashAnim1前面开始fallAnim动画
            bouncer.play(fallAnim).before(squashAnim1);
            //squashAnim1 与 squashAnim2、stretchAnim1、stretchAnim2一起开始
            bouncer.play(squashAnim1).with(squashAnim2);
            bouncer.play(squashAnim1).with(stretchAnim1);
            bouncer.play(squashAnim1).with(stretchAnim2);
            //在stretchAnim2动画后开始bounceBackAnim
            bouncer.play(bounceBackAnim).after(stretchAnim2);


            //定义alpha属性，从1到0的动画（渐隐动画）
            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f);

            fadeAnim.addListener(new AnimatorListenerAdapter() {
                //当动画结束时
                @Override
                public void onAnimationEnd(Animator animation) {
                    //动画结束时将该动画关联的ShapeHolder删除
                    balls.remove(((ObjectAnimator) animation).getTarget());
                }
            });
            fadeAnim.addUpdateListener(this);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(bouncer).before(fadeAnim);
            animatorSet.start();
            return true;

        }

        private ShapeHolder addBall(float x, float y) {

            //创建一个椭圆
            OvalShape circle = new OvalShape();
            circle.resize(BALL_SIZE, BALL_SIZE);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x - BALL_SIZE / 2);
            shapeHolder.setY(y - BALL_SIZE / 2);
            int red = (int) (Math.random() * 255);
            int green = (int) (Math.random() * 255);
            int blue = (int) (Math.random() * 255);
            int color = 0xff000000 + red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint();
            int darkColor = 0xff000000 | red / 4 << 16 | green / 4 << 8 | blue / 4;
            // 第一个,第二个参数表示渐变起点 可以设置起点终点在对角等任意位置
            // 第三个,第四个参数表示起初颜色和最终颜色
            // 第五个参数表示平铺方式
            // CLAMP重复最后一个颜色至最后
            // MIRROR重复着色的图像水平或垂直方向已镜像方式填充会有翻转效果
            // REPEAT重复着色的图像水平或垂直方向
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f, BALL_SIZE, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            balls.add(shapeHolder);
            return shapeHolder;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            for (ShapeHolder shapeHolder : balls) {
                canvas.save();
                canvas.translate(shapeHolder.getX(), shapeHolder.getY());
                shapeHolder.getShapeDrawable().draw(canvas);
                canvas.restore();
            }


        }
    }


    class ShapeHolder {
        private float x = 0, y = 0;
        private ShapeDrawable shapeDrawable;
        private int color;
        private RadialGradient radialGradient;
        private float alpha = 1f;
        private Paint paint;

        public float getWidth() {
            return shapeDrawable.getShape().getWidth();
        }

        public void setWidth(float width) {
            Shape s = shapeDrawable.getShape();
            s.resize(width, s.getHeight());
        }

        public float getHeitht() {
            return shapeDrawable.getShape().getHeight();
        }

        public void setHeight(float height) {
            Shape s = shapeDrawable.getShape();
            s.resize(s.getWidth(), height);
        }

        public ShapeHolder(ShapeDrawable shapeDrawable) {
            this.shapeDrawable = shapeDrawable;
        }

        public Paint getPaint() {
            return paint;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }

        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public RadialGradient getRadialGradient() {
            return radialGradient;
        }

        public void setRadialGradient(RadialGradient radialGradient) {
            this.radialGradient = radialGradient;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public ShapeDrawable getShapeDrawable() {
            return shapeDrawable;
        }

        public void setShapeDrawable(ShapeDrawable shapeDrawable) {
            this.shapeDrawable = shapeDrawable;
        }


    }
}
