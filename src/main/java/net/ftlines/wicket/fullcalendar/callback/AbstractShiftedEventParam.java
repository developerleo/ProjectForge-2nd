/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.ftlines.wicket.fullcalendar.callback;

import net.ftlines.wicket.fullcalendar.Event;
import net.ftlines.wicket.fullcalendar.EventSource;

import org.joda.time.DateTime;
import org.joda.time.Period;

class AbstractShiftedEventParam extends AbstractEventParam {
	private final int daysDelta;
	private final int minutesDelta;

	public AbstractShiftedEventParam(EventSource source, Event event, int hoursDelta, int minutesDelta) {
		super(source, event);
		this.daysDelta = hoursDelta;
		this.minutesDelta = minutesDelta;
	}

	public int getDaysDelta() {
		return daysDelta;
	}

	public int getMinutesDelta() {
		return minutesDelta;
	}

	public DateTime getNewStartTime() {
		return shift(getEvent().getStart());
	}

	public DateTime getNewEndTime() {
		return shift(getEvent().getEnd());
	}

	public Period getDelta() {
		return Period.days(daysDelta).plusMinutes(minutesDelta);
	}

	private DateTime shift(DateTime start) {
		return start.plusDays(daysDelta).plusMinutes(minutesDelta);
	}

}
