package com.demo.smarthome.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import com.demo.smarthome.R;

/**********************************************************
 * @文件作者：sl
 * @文件描述：画出曲线图
 * @创建日期:2015-11-07
 * 注意 :里面代码逻辑较为复杂,非必要请不要修改
 **********************************************************/
public class HistoryDataLineView extends View {

	private static final int CIRCLE_SIZE = 10;
	//连接线是曲线还是直线
	private static enum Linestyle {
		Line, Curve
	}

	private Context mContext;
	private Paint mPaint;
	private Resources res;
	private DisplayMetrics dm;
	private boolean isInitDatafinish = false;
	/**
	 * data
	 */
	private Linestyle mStyle = Linestyle.Curve;

	private int canvasHeight;
	private int canvasWidth;
	private int bheight = 0;
	//左右宽度留的边
	private int blwidh;
	private boolean isMeasure = true;
	//如果是PM2.5或者PM10,置真
	boolean pm2_5Flag;
	/**
	 * Y轴最大值
	 */
	private float maxValue;
	/**
	 * Y轴间距值
	 */
	private float averageValue;


	//X轴标示微调
	private static final int xTextChange =5;
	//Y轴标示微调
	private static final int yTextChange =5;
	//Y轴单位位置调整,向下的程度
	private static final int yTextUnit = 10;

	//X轴坐标数从零点到24点,每两小时一个点
	private static final int xSpaceCount = 13;

	//顶部底部留白
	private int marginTop = 50;
	private int marginBottom = 100;


	//Y轴单位
	private String yUnit;

	//笔的粗细程度
	private static final float mPaintWidth = 2.0f;
	//是否显示坐标的小点
	private static final boolean enablePaintPoint = false;
	/**
	 * 曲线上总点数
	 */
	private Point[] mPoints;
	/**
	 * 纵坐标值
	 */
	private ArrayList<Double> yRawData;
	/**
	 * 横坐标值
	 */

	private int spacingHeight;

	public HistoryDataLineView(Context context)
	{
		this(context, null);
	}

	public HistoryDataLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initView();
	}

	private void initView() {
		this.res = mContext.getResources();
		//抗锯齿效果
		this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//获取分辨率的类
		dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		if (isMeasure) {
			this.canvasHeight = getHeight();
			this.canvasWidth = getWidth();
			if (bheight == 0)
				bheight = (int) (canvasHeight - marginBottom);
			//左右两边留白
			blwidh = dip2px(30);
			isMeasure = false;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		//初始化未完成不画曲线
		if(!isInitDatafinish){
			return;
		}
		//已在android6.0版本被废弃,有时间要修改一下

		// 画直线（横向和纵向）,坐标标示随着类型不同会改变
		drawAllXLine(canvas);
		drawAllYLine(canvas);
		// 点的操作设置
		mPoints = getPoints();

		mPaint.setColor(res.getColor(R.color.viewfinder_laser));
		mPaint.setStrokeWidth(dip2px(mPaintWidth));
		mPaint.setStyle(Style.STROKE);
		//曲线还是直线
		if (mStyle == Linestyle.Curve) {

			drawScrollLine(canvas);
		}
		else {
			drawLine(canvas);
		}

		if(enablePaintPoint){
			mPaint.setStyle(Style.FILL);
			for (int i = 0; i < mPoints.length; i++) {

				canvas.drawCircle(mPoints[i].x, mPoints[i].y, CIRCLE_SIZE / 2, mPaint);
			}
		}
	}

	/**
	 *  画所有横向表格，包括X轴
	 */
	private void drawAllXLine(Canvas canvas) {

		for (int i = 0; i < spacingHeight + 1; i++) {
			//轴坐标线颜色不一样
			if( i == 0){
				mPaint.setColor(res.getColor(R.color.sbc_snippet_text));
			}else{

				mPaint.setColor(res.getColor(R.color.help_button_view));
			}

			canvas.drawLine(blwidh, bheight - (bheight / spacingHeight) * i + marginTop, canvasWidth - blwidh,
					bheight - (bheight / spacingHeight) * i + marginTop, mPaint);


			//如果是PM2.5或者PM10,显示的坐标标示不带小数
			if( i!= 0) {
				if (pm2_5Flag) {
					int intAverage = (int) averageValue;
					drawText(String.valueOf(intAverage * i), blwidh / 2 - dip2px(yTextChange + 2)
							, bheight - (bheight / spacingHeight) * i + marginTop + dip2px(yTextChange), canvas);
				} else {
					drawText(String.valueOf(averageValue * i), blwidh / 2 - dip2px(yTextChange)
							, bheight - (bheight / spacingHeight) * i + marginTop + dip2px(yTextChange), canvas);
				}
			}
		}
		//显示单位
		drawText(String.valueOf("(" + yUnit + ")"), blwidh / 2 - dip2px(yTextChange) - dip2px(5)
				,  dip2px(yTextUnit), canvas);
	}

	/**
	 * 画所有纵向表格，包括Y轴
	 */
	private void drawAllYLine(Canvas canvas) {

		for (int i = 0; i < xSpaceCount; i++) {

			//轴坐标线颜色不一样
			if( i == 0){
				mPaint.setColor(res.getColor(R.color.sbc_snippet_text));
			}else{

				mPaint.setColor(res.getColor(R.color.help_button_view));
			}

			canvas.drawLine(blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, marginTop, blwidh
					+ (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, bheight + marginTop, mPaint);
			//当坐标标示是两位数的画多向左移动一点,为了美观.X轴和Y轴公用0点,0点也要左移
			//最后一个坐标要加(时)
			if(i == xSpaceCount - 1){
				drawText(String.valueOf(i*2) + "(时)", blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						- dip2px(xTextChange), bheight + dip2px(35), canvas);
			}else if(i == 0 || i > 9){
				drawText(String.valueOf(i*2), blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						- dip2px(xTextChange), bheight + dip2px(35), canvas);
			}
			else {
				drawText(String.valueOf(i*2), blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						, bheight + dip2px(35), canvas);
			}

		}
	}

	private void drawScrollLine(Canvas canvas) {

		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < mPoints.length - 1; i++) {

			startp = mPoints[i];
			endp = mPoints[i + 1];
			int wt = (startp.x + endp.x) / 2;
			Point p3 = new Point();
			Point p4 = new Point();
			p3.y = startp.y;
			p3.x = wt;
			p4.y = endp.y;
			p4.x = wt;

			Path path = new Path();
			path.moveTo(startp.x, startp.y);
			path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
			canvas.drawPath(path, mPaint);
		}
	}

	private void drawLine(Canvas canvas) {

		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < mPoints.length - 1; i++) {

			startp = mPoints[i];
			endp = mPoints[i + 1];
			canvas.drawLine(startp.x, startp.y, endp.x, endp.y, mPaint);
		}
	}

	private void drawText(String text, int x, int y, Canvas canvas) {

		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setTextSize(dip2px(12));
		p.setColor(res.getColor(R.color.sbc_header_text));
		p.setTextAlign(Paint.Align.LEFT);
		canvas.drawText(text, x, y, p);
	}

	private Point[] getPoints() {

		int Xvalue;
		double xPointSpace;
		Point[] points = new Point[yRawData.size()];
		for (int i = 0; i < yRawData.size(); i++) {

			int ph = bheight - (int) (bheight * (yRawData.get(i) /(int) maxValue));
			//1080P手机分辨率除288如果结果时整型会极为不准确故显示double
			xPointSpace = (double)(canvasWidth - 2*blwidh) / (yRawData.size()-1);
			Xvalue = (int)(blwidh + xPointSpace * i);

			points[i] = new Point(Xvalue, ph + marginTop);
		}
		return points;
	}

	public void setData(ArrayList<Double> yRawData, float maxValue, float averageValue,boolean pmFlag) {

		this.maxValue = maxValue;
		this.averageValue = averageValue;
		this.mPoints = new Point[yRawData.size()];
		this.yRawData = yRawData;
		this.spacingHeight = (int)(maxValue / averageValue);
		isInitDatafinish = true;
		this.pm2_5Flag = pmFlag;
		//重新调用onDraw
		postInvalidate();
	}

	public void setTotalvalue(float maxValue)
	{
		this.maxValue = maxValue;
	}

	public void setPjvalue(int averageValue)
	{
		this.averageValue = averageValue;
	}

	public void setMargint(int marginTop)
	{
		this.marginTop = marginTop;
	}

	public void setMarginb(int marginBottom)
	{
		this.marginBottom = marginBottom;
	}

	public void setMstyle(Linestyle mStyle)
	{
		this.mStyle = mStyle;
	}

	public void setBheight(int bheight)
	{
		this.bheight = bheight;
	}

	public void setyUnit(String yUnit) {
		this.yUnit = yUnit;
	}
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	private int dip2px(float dpValue)
	{
		return (int) (dpValue * dm.density + 0.5f);
	}

}
