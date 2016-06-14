/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.content.actions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.content.Look;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.InterpretationException;
import org.catrobat.catroid.stage.StageActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StampAction extends TemporalAction {

	private Sprite sprite;
	private int counter = 0;

	@Override
	protected void update(float delta) {
		counter++;
		Project project = ProjectManager.getInstance().getCurrentProject();
		Sprite background = project.getSpriteList().get(0);

		LookData lookData = background.look.getLookData();

		Pixmap pm = new Pixmap(0,0, Pixmap.Format.RGBA8888);

		float xCoord =  sprite.look.getLookData().getPixmap().getWidth() / 2f;
		float yCoord =  sprite.look.getLookData().getPixmap().getHeight() / 2f;

		int x = lookData.getPixmap().getWidth()/2-(int)xCoord + (int)this.sprite.look.getXInUserInterfaceDimensionUnit();
		int y = lookData.getPixmap().getHeight()/2-(int)yCoord - (int)this.sprite.look.getYInUserInterfaceDimensionUnit();

		double scale = sprite.look.getSizeInUserInterfaceDimensionUnit()/100;

		if (scale < 1) {
			x = (int) (x + sprite.look.getLookData().getPixmap().getWidth()*(1-scale)*0.5);
			y = (int) (y + sprite.look.getLookData().getPixmap().getHeight()*(1-scale)*0.5);
		}
		else if(scale > 1) {
			x = (int) (x - sprite.look.getLookData().getPixmap().getWidth()*(scale-1)*0.5);
			y = (int) (y - sprite.look.getLookData().getPixmap().getHeight()*(scale-1)*0.5);
		}

		if(sprite.look.getDirectionInUserInterfaceDimensionUnit() != 90) {

			// Draw rotated matrix

			Polygon poly = new Polygon(new float[]{
					0,0,
					this.sprite.look.getLookData().getPixmap().getWidth()*(float)scale,0,
					this.sprite.look.getLookData().getPixmap().getWidth()*(float)scale, this.sprite.look.getLookData().getPixmap().getHeight()*(float)scale,
					0,this.sprite.look.getLookData().getPixmap().getHeight()*(float)scale
			});

			float angle = sprite.look.getDirectionInUserInterfaceDimensionUnit()-90;

			Float polyRotation = angle;

			angle = 360 - angle;

			pm = new Pixmap(((int)((double)this.sprite.look.getLookData().getPixmap().getWidth()*scale)),
					((int)((double)this.sprite.look.getLookData().getPixmap().getHeight()*scale)),
			Pixmap.Format.RGBA8888);

			pm.drawPixmap(this.sprite.look.getLookData().getPixmap(), 0,0, (int)this.sprite.look.getLookData()
					.getPixmap().getWidth(), (int)this.sprite.look.getLookData().getPixmap().getHeight(), 0,0,
					pm.getWidth(),pm.getHeight());

			poly.setRotation(polyRotation);

			Rectangle rect = poly.getBoundingRectangle();

			double xCorrection = (rect.getWidth() - pm.getWidth())/2;
			double yCorrection = (rect.getHeight() - pm.getHeight())/2;

			x -= xCorrection;
			y -= yCorrection;


			final int width = ((int) pm.getWidth());
			final int height = ((int) pm.getHeight());

			final int newWidth = (int) rect.getWidth();
			final int newHeight = (int) rect.getHeight();
			final int newCenterX = newWidth / 2;
			final int newCenterY = newHeight / 2;

			final double radians = Math.toRadians(angle), cos = Math.cos(radians), sin = Math.sin(radians);
			int counter = 0;

			double centerx, centery, m, n;
			long p,q;

			int j,k;

		for (int w = 0; w < newWidth; w++) {
			for (int h = 0; h < newHeight; h++) {
				centerx = width/2;
				centery = height/2;
				m = w - newCenterX;
				n = h - newCenterY;

				p = Math.round((m*cos-n*sin) + centerx);
				q = Math.round((n*cos + (m*sin)) + centery);

				j = (int)p;
				k = (int)q;



				if (j >= 0 && j < width && k >= 0 && k < height) {
					lookData.getPixmap().drawPixel(w+x, h+y, pm.getPixel(j, k));
				}
			}
		}

		}
		else {
			lookData.getPixmap().drawPixmap(this.sprite.look.getLookData().getPixmap(),
					0,
					0,
					this.sprite.look.getLookData().getPixmap().getWidth(),
					this.sprite.look.getLookData().getPixmap().getHeight(),
					x,
					y,
					(int) ((float) this.sprite.look.getLookData().getPixmap().getWidth() / 100 * (sprite.look.getSizeInUserInterfaceDimensionUnit())),
					(int) ((float) this.sprite.look.getLookData().getPixmap().getHeight() / 100 * (sprite.look.getSizeInUserInterfaceDimensionUnit
							())));


		}

		pm.dispose();

		TextureRegion textureRegion = lookData.getTextureRegion();
		textureRegion.getTexture().draw(lookData.getPixmap(),0,0);

		background.look.setLookData(lookData);
	}

	public Pixmap rotatePixmap (Pixmap src, float angle){

		Polygon poly = new Polygon(new float[]{
				0,0,
				src.getWidth(),0,
				src.getWidth(), src.getHeight(),
				0,src.getHeight()
		});

		angle = angle-90;

		Float polyRotation = angle;

		angle = 360 - angle;


		Log.v("SIZE", "width: " + src.getWidth());
		Log.v("SIZE", "height: " + src.getHeight());
		Log.v("SIZE", "width2: " + poly.getBoundingRectangle().getWidth());
		Log.v("SIZE", "width2: " + poly.getBoundingRectangle().getHeight());
		Log.v("SIZE", "rotation: " + polyRotation);
		poly.setRotation(polyRotation);

		Rectangle rect = poly.getBoundingRectangle();

		Log.v("SIZE", "RectWidth:" + rect.getWidth());
		Log.v("SIZE", "RectHeight:" + rect.getHeight());

		//final int width = ((int)rect.getWidth());
		//final int height = ((int) rect.getHeight());

		final int width = ((int) src.getWidth());
		final int height = ((int) src.getHeight());

		Log.v("StampFormat","format: " + src.getFormat().toString());
		Log.v("StampFormat","formatValues:" + src.getFormat().values());

		final int newWidth = (int) rect.getWidth();
		final int newHeight = (int) rect.getHeight();
		final int newCenterX = newWidth / 2;
		final int newCenterY = newHeight / 2;

		Pixmap rotated = new Pixmap(newWidth, newHeight, src.getFormat());
/*
		for(int i = 0; i<rotated.getWidth(); i++) {
			for(int j = 0; j<rotated.getHeight(); j++) {
				rotated.drawPixel(i,j,Color.RED);
			}
		}
*/

		int[][] drawArray = new int[newWidth+1][newHeight+1];

		for(int i = 0; i<=newWidth; i++) {
			for(int j = 0; j<=newHeight; j++) {
				drawArray[i][j] = 0;
			}
		}

		final double radians = Math.toRadians(angle), cos = Math.cos(radians), sin = Math.sin(radians);


		Log.v("Stamp", "NewWidth: " + newWidth);
		Log.v("Stamp", "NewHeight: " + newHeight);
		Log.v("Stamp", "NewCenterX: " + newCenterX);
		Log.v("Stamp", "NewCenterY: " + newCenterY);

		int counter = 0;
		int counter2 = 0;
		int counter3 = 0;

		int[] test = new int[250000];
		for(int  i= 0; i<test.length; i++) {
			test[i] = 5000;
		}
		Log.v("TESTER", "test: "+test[14820]);
		Log.v("COS", "cos: " + cos);
		Log.v("SIN", "sin: " + sin);
		//for (int x = 0; x < width; x++) {
			for (int x = 0; x < 1; x++) {
			//for (int y = 0; y < height; y++) {
				for (int y = 0; y < 1; y++) {
				double centerx = width/2;
				double centery = height/2;
				double m = x - centerx;
				double n = y - centery;
				long p = Math.round((m*cos+n*sin) + newCenterX);
				long q = Math.round((n*cos - (m*sin)) + newCenterY);

				int j = (int)p;
				int k = (int)q;

				Log.v("GETVALUES", "j:" + p);
				Log.v("GETVALUES", "k:" + q);

				//final int
						//centerx = width/2, centery = height / 2,
						//m = x - centerx,
						//n = y - centery,
						//j = ((int) (m * cos + n * sin)) + newCenterX,
						//k = ((int) (n * cos - (m * sin))) + newCenterY;
				//Log.v("Stamp", "m: " + m);
				//Log.v("Stamp", "n: " + n);
				//rotated.drawPixel(x,y, Color.RED);
				/*
				if(j<k) {
					int a = j*k;
					/*if(a == 14883){
						Log.v("AUFSCHREI", "j: " + j + " k: " + k);
						Log.v("AUFSCHREI", "x: " + x + " y: " + y);
						double u = ((m * cos + n * sin)) + newCenterX;
						double v = (n * cos - (m * sin)) + newCenterY;
						Log.v("AUFSCHREI", "u: " + u + " v: " + v);
					}
					if(test[a] != 5000) {
						counter3++;
						double u = ((m * cos + n * sin)) + newCenterX;
						double v = (n * cos - (m * sin)) + newCenterY;
						Log.v("AUFSCHREI", "j: " + j + " k: " + k + " u: "+ u+" v: "+v);
					}
					test[a] = 1;

				}*/
				if (j >= 0 && j < newWidth && k >= 0 && k < newHeight){
					//rotated.drawPixel(x, y, src.getPixel(k, j));
					rotated.drawPixel(j, k, src.getPixel(x, y));
					counter++;
					if(drawArray[j][k] == 0) {
						drawArray[j][k] = 1;
					}
					else {
						counter3++;
						if((j+1)<newWidth) {
							counter2++;
							if(drawArray[j+1][k] == 0) {
								rotated.drawPixel(j + 1, k, src.getPixel(x, y));
							}
						}
						if((k+1)<newHeight) {
							if(drawArray[j][k+1] == 0) {
								rotated.drawPixel(j, k + 1, src.getPixel(x, y));
							}
						}
						if((k-1)>=0) {
							if(drawArray[j][k-1] == 0) {
								rotated.drawPixel(j, k-1, src.getPixel(x, y));
							}
						}
						if((j-1)>=0) {
							if(drawArray[j-1][k] == 0) {
								rotated.drawPixel(j-1, k, src.getPixel(x, y));
							}
						}
						if((j+1)<newWidth && (k+1)<newHeight) {
							if(drawArray[j+1][k+1] == 0) {
								rotated.drawPixel(j+1, k + 1, src.getPixel(x, y));
							}
						}
						if((j-1)>=0 && (k-1)>=0) {
							if(drawArray[j-1][k-1] == 0) {
								rotated.drawPixel(j-1, k - 1, src.getPixel(x, y));
							}
						}
						if((j-1)>=0 && (k+1)<newHeight) {
							if(drawArray[j-1][k+1] == 0) {
								rotated.drawPixel(j-1, k+1, src.getPixel(x, y));
							}
						}
						if((j+1)< newWidth && (k-1)>=0) {
							if(drawArray[j+1][k-1] == 0) {
								rotated.drawPixel(j+1, k - 1, src.getPixel(x, y));
							}
						}

					}
				}
				else if(j < 0) {
					Log.v("Stamp", "------------ OUT OF BOUNDS j<0 -----------#x="+x+"##y="+y+"##j"+j);
				}
				else if(j > newWidth) {
					Log.v("Stamp", "------------ OUT OF BOUNDS j > newWidth -----------#x="+x+"##y="+y+"##j"+j);
				}
				else if(k < 0) {
					Log.v("Stamp", "------------ OUT OF BOUNDS k < 0 -----------#x="+x+"##y="+y+"##k"+k);
				}
				else if(k > newHeight) {
					Log.v("Stamp", "------------ OUT OF BOUNDS k > newHeight ------------#x="+x+"##y="+y+"##k"+k);
				}
				else {
					Log.v("StampOOOOOUUUTTT", "Falsch");
				}
			}
		}
		Log.v("Counter", "counter: " + counter);
		Log.v("Counter2", "counter2: " + counter2);
		Log.v("Counter3", "counter3: " + counter3);
		return rotated;

	}

	public Pixmap rotatePixmap2 (Pixmap src, float angle){

		Polygon poly = new Polygon(new float[]{
				0,0,
				src.getWidth(),0,
				src.getWidth(), src.getHeight(),
				0,src.getHeight()
		});

		angle = angle-90;

		Float polyRotation = angle;

		angle = 360 - angle;


		poly.setRotation(polyRotation);

		Rectangle rect = poly.getBoundingRectangle();

		final int width = ((int) src.getWidth());
		final int height = ((int) src.getHeight());

		final int newWidth = (int) rect.getWidth();
		final int newHeight = (int) rect.getHeight();
		final int newCenterX = newWidth / 2;
		final int newCenterY = newHeight / 2;

		//Pixmap rotated = new Pixmap(newWidth, newHeight, src.getFormat());
		Pixmap rotated = src;
		final double radians = Math.toRadians(angle), cos = Math.cos(radians), sin = Math.sin(radians);
		int counter = 0;

		double centerx, centery, m, n;
		long p,q;

		int j,k;

		/*
		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				centerx = width/2;
				centery = height/2;
				m = x - newCenterX;
				n = y - newCenterY;

				p = Math.round((m*cos-n*sin) + centerx);
				q = Math.round((n*cos + (m*sin)) + centery);

				j = (int)p;
				k = (int)q;



				if (j >= 0 && j < width && k >= 0 && k < height) {
					rotated.drawPixel(x, y, src.getPixel(j, k));
				}
			}
		}
		*/
		return rotated;

	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

}
