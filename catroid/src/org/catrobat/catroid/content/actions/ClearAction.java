/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2015 The Catrobat Team
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

import android.graphics.Color;
import android.util.Log;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;

public class ClearAction extends TemporalAction {

	//private Sprite sprite;

	@Override
	protected void update(float delta) {
		Log.v("Clear", "do update");
		Project project = ProjectManager.getInstance().getCurrentProject();
		Sprite background = project.getSpriteList().get(0);

		LookData lookData = background.look.getLookData();

		if(lookData != null) {

			lookData.setPixmap(lookData.getOriginalPixmap());

			lookData.setTextureRegion();

			lookData.resetLookData();

			background.look.setLookData(lookData);
		}
		else {
			Log.v("Clear", "lookData is null!");
		}



	}
	// Wird das Sprite benötigt? Wenn nur der ganze Hintergrund zrückgesetzt wird nicht..

	//public void setSprite(Sprite sprite) {
		//this.sprite = sprite;
	//}

}
