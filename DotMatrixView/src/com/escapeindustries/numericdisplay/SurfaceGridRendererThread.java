package com.escapeindustries.numericdisplay;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

public class SurfaceGridRendererThread extends Thread {

	private static final int TRANSITION_LIMIT = 400;
	private static final int LIGHTENING = 2;
	private static final int DIMMING = 1;
	private static final int LIT = 3;
	private static final int DIM = 0;
	private boolean running;
	private SurfaceHolder holder;
	private Context context;
	private ModelGrid grid;
	private int litColor;
	private Paint lit;
	private int dimColor;
	private Paint dim;
	private int rows = 13;
	private int columns = 7;
	private int[][] coordsX;
	private int[][] coordsY;
	private int radius = 4;
	private int space = 2;

	private int lastSeconds = -1;
	private int fps = 0;
	private Paint[] paints = new Paint[4];
	private long sinceSecond;

	private static String TAG = "NumericalDisplay";
	private int alphaRange;
	private int redRange;
	private int blueRange;
	private int greenRange;
	private int litAlpha;
	private int dimAlpha;
	private int litRed;
	private int dimRed;
	private int litBlue;
	private int dimBlue;
	private int litGreen;
	private int dimGreen;

	public SurfaceGridRendererThread(SurfaceHolder holder, Context context,
			ModelGrid grid) {
		this.holder = holder;
		this.context = context;
		this.grid = grid;
		setupColors();
		this.columns = grid.getColumns();
		this.rows = grid.getRows();
		initCoords();
	}

	@Override
	public void run() {
		while (running) {
			updatePaints();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				if (canvas != null) {
					synchronized (holder) {
						canvas.drawColor(Color.BLACK);
						for (int row = 0; row < rows; row++) {
							for (int column = 0; column < columns; column++) {
								canvas.drawCircle(coordsX[row][column],
										coordsY[row][column], radius,
										paints[grid.getDotState(column, row)]);
							}
						}
					}
					logFPS();
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	private long getStartOfSecond(Calendar now) {
		Calendar from = GregorianCalendar.getInstance();
		from.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
				now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
		from.set(Calendar.MILLISECOND, 0);
		return from.getTimeInMillis();
	}

	private long getMillisSinceSecond() {
		Calendar now = GregorianCalendar.getInstance();
		return now.getTimeInMillis() - getStartOfSecond(now);
	}

	private void logFPS() {
		int nowSeconds = GregorianCalendar.getInstance().get(Calendar.SECOND);
		if (nowSeconds != lastSeconds) {
			Log.d(TAG, "SurfaceGridRendererThread: fps == " + fps);
			fps = 1;
			lastSeconds = nowSeconds;
		} else {
			fps++;
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	private void setupColors() {
		dimColor = context.getResources().getColor(R.color.dim_green);
		litColor = context.getResources().getColor(R.color.bright_green);
		setUpColorComponents();
		setUpColorRanges();
		paints[DIM] = getDim();
		paints[DIMMING] = getDimming();
		paints[LIGHTENING] = getLightening();
		paints[LIT] = getLit();
	}

	private void updatePaints() {
		sinceSecond = getMillisSinceSecond();
		paints[DIMMING] = getDimming();
		paints[LIGHTENING] = getLightening();
	}

	private Paint getPaint(int color) {
		Paint p = new Paint();
		p.setColor(color);
		return p;
	}

	private Paint getDim() {
		return getPaint(dimColor);
	}
	
	private Paint getLit() {
		return getPaint(litColor);
	}

	private Paint getDimming() {
		Paint result;
		if (sinceSecond >= TRANSITION_LIMIT) {
			result = paints[DIM];
		} else {
			float percentThroughTransition = 1.0f - ((float)sinceSecond / TRANSITION_LIMIT);
			result = new Paint();
			result.setColor(getInterstitial(percentThroughTransition));
		}
		return result;
	}

	private Paint getLightening() {
		Paint result;
		if (sinceSecond >= TRANSITION_LIMIT) {
			result = paints[LIT];
		} else {
			float percentThroughTransition = (float)sinceSecond / TRANSITION_LIMIT;
			result = new Paint();
			result.setColor(getInterstitial(percentThroughTransition));
		}
		return result;
	}

	private int getInterstitial(float percentThroughTransition) {
		return Color.argb(dimAlpha
				+ (int)(alphaRange * percentThroughTransition), dimRed
				+ (int)(redRange * percentThroughTransition), dimGreen
				+ (int)(greenRange * percentThroughTransition), dimBlue
				+ (int)(blueRange * percentThroughTransition));
	}

	private void setUpColorComponents() {
		litAlpha = Color.alpha(litColor);
		dimAlpha = Color.alpha(dimColor);
		litRed = Color.red(litColor);
		dimRed = Color.red(dimColor);
		litBlue = Color.blue(litColor);
		dimBlue = Color.blue(dimColor);
		litGreen = Color.green(litColor);
		dimGreen = Color.green(dimColor);
	}

	private void setUpColorRanges() {
		alphaRange = litAlpha - dimAlpha;
		redRange = litRed - dimRed;
		blueRange = litBlue - dimBlue;
		greenRange = litGreen - dimGreen;
	}

	private void initCoords() {
		// Set up a pair of 2D arrays. Each holds one half of the coordinates
		// for every dot in the grid
		coordsX = new int[rows][columns];
		coordsY = new int[rows][columns];
		int rowStart = radius + space;
		int x = rowStart;
		int y = rowStart;
		int centerSpacing = space + radius * 2;
		// Iterate over all rows and columns
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				coordsX[row][column] = x;
				coordsY[row][column] = y;
				x = x + centerSpacing;
			}
			// End of a row - move down and back to left
			y = y + centerSpacing;
			x = rowStart;
		}
	}
}