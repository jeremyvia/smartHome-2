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
 * @�ļ����ߣ�sl
 * @�ļ���������������ͼ
 * @��������:2015-11-07
 * ע��?:��������߼���Ϊ����,�Ǳ�Ҫ�벻Ҫ�޸�
 **********************************************************/
public class HistoryDataLineView extends View {

	private static final int CIRCLE_SIZE = 10;
	//�����������߻���ֱ��
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
	//���ҿ������ı�
	private int blwidh;
	private boolean isMeasure = true;
	//�����PM2.5����PM10,����
	boolean pm2_5Flag;
	/**
	 * Y�����ֵ
	 */
	private float maxValue;
	/**
	 * Y����ֵ
	 */
	private float averageValue;


	//X���ʾ΢��
	private static final int xTextChange =5;
	//Y���ʾ΢��
	private static final int yTextChange =5;
	//Y�ᵥλλ�õ���,���µĳ̶�
	private static final int yTextUnit = 10;

	//X������������㵽24��,ÿ��Сʱһ����
	private static final int xSpaceCount = 13;

	//�����ײ�����
	private int marginTop = 50;
	private int marginBottom = 100;


	//Y�ᵥλ
	private String yUnit;

	//�ʵĴ�ϸ�̶�
	private static final float mPaintWidth = 2.0f;
	//�Ƿ���ʾ�����С��
	private static final boolean enablePaintPoint = false;
	/**
	 * �������ܵ���
	 */
	private Point[] mPoints;
	/**
	 * ������ֵ
	 */
	private ArrayList<Double> yRawData;
	/**
	 * ������ֵ
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
		//�����Ч��
		this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//��ȡ�ֱ��ʵ���
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
			//������������
			blwidh = dip2px(30);
			isMeasure = false;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		//��ʼ��δ��ɲ�������
		if(!isInitDatafinish){
			return;
		}
		//����android6.0�汾������,��ʱ��Ҫ�޸�һ��

		// ��ֱ�ߣ����������,�����ʾ�������Ͳ�ͬ��ı�
		drawAllXLine(canvas);
		drawAllYLine(canvas);
		// ��Ĳ�������
		mPoints = getPoints();

		mPaint.setColor(res.getColor(R.color.viewfinder_laser));
		mPaint.setStrokeWidth(dip2px(mPaintWidth));
		mPaint.setStyle(Style.STROKE);
		//���߻���ֱ��
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
	 *  �����к�����񣬰���X��
	 */
	private void drawAllXLine(Canvas canvas) {

		for (int i = 0; i < spacingHeight + 1; i++) {
			//����������ɫ��һ��
			if( i == 0){
				mPaint.setColor(res.getColor(R.color.sbc_snippet_text));
			}else{

				mPaint.setColor(res.getColor(R.color.help_button_view));
			}

			canvas.drawLine(blwidh, bheight - (bheight / spacingHeight) * i + marginTop, canvasWidth - blwidh,
					bheight - (bheight / spacingHeight) * i + marginTop, mPaint);


			//�����PM2.5����PM10,��ʾ�������ʾ����С��
			if (pm2_5Flag) {
				int intAverage = (int) averageValue;
				drawText(String.valueOf(intAverage * i), blwidh / 2 - dip2px(yTextChange + 2)
						, bheight - (bheight / spacingHeight) * i + marginTop + dip2px(yTextChange), canvas);
			} else {
				drawText(String.valueOf(averageValue * i), blwidh / 2 - dip2px(yTextChange)
						, bheight - (bheight / spacingHeight) * i + marginTop + dip2px(yTextChange), canvas);
			}
		}
		//��ʾ��λ
		drawText(String.valueOf("(" + yUnit + ")"), blwidh / 2 - dip2px(yTextChange) - dip2px(5)
				,  dip2px(yTextUnit), canvas);
	}

	/**
	 * ������������񣬰���Y��
	 */
	private void drawAllYLine(Canvas canvas) {

		for (int i = 0; i < xSpaceCount; i++) {

			//����������ɫ��һ��
			if( i == 0){
				mPaint.setColor(res.getColor(R.color.sbc_snippet_text));
			}else{

				mPaint.setColor(res.getColor(R.color.help_button_view));
			}

			canvas.drawLine(blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, marginTop, blwidh
					+ (canvasWidth - 2 * blwidh) / (xSpaceCount - 1) * i, bheight + marginTop, mPaint);
			//�������ʾ����λ���Ļ��������ƶ�һ��,Ϊ������.X���Y�ṫ��0��,0��ҲҪ����
			//���һ������Ҫ��(ʱ)
			if(i == xSpaceCount - 1){
				drawText(String.valueOf(i*2) + "(ʱ)", blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						- dip2px(xTextChange), bheight + dip2px(30), canvas);
			}else if(i == 0 || i > 9){
				drawText(String.valueOf(i*2), blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						- dip2px(xTextChange), bheight + dip2px(30), canvas);
			}
			else {
				drawText(String.valueOf(i*2), blwidh + (canvasWidth - 2 * blwidh) / (xSpaceCount-1) * i
						, bheight + dip2px(30), canvas);
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
			//1080P�ֻ��ֱ��ʳ�288������ʱ���ͻἫΪ��׼ȷ����ʾdouble
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
		//���µ���onDraw
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
	 * �����ֻ��ķֱ��ʴ� dp �ĵ�λ ת��Ϊ px(����)
	 */
	private int dip2px(float dpValue)
	{
		return (int) (dpValue * dm.density + 0.5f);
	}

}