package com.tinysine.lazyboneble;

import android.graphics.Point;

public class Tools {
	public static double getAngle(Point current) {
		if (current.x >= 0 && current.y >= 0) {
			if (current.x == 0 && current.y == 0)
				return 0;
			if (current.x == 0 && current.y > 0)
				return 90;
			if (current.x > 0 && current.y == 0)
				return 180;
			return Math.atan2(current.y, current.x) / 3.14 * 180;
		} else if (current.x >= 0 && current.y < 0) {
			if (current.x == 0 && current.y < 0)
				return 270;
			return Math.atan2(current.y, current.x) / 3.14 * 180 + 360;
		} else if (current.x < 0 && current.y >= 0) {
			if (current.x < 0 && current.y == 0)
				return 180;
			return Math.atan2(current.y, current.x) / 3.14 * 180;
		} else {
			return Math.atan2(current.y, current.x) / 3.14 * 180 + 360;
		}

	}
}
