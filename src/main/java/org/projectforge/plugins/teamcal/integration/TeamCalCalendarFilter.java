/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.projectforge.user.PFUserContext;
import org.projectforge.web.calendar.AbstractCalendarFilter;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalCalendarFilter extends AbstractCalendarFilter
{
  private static final long serialVersionUID = 7361308619781209753L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalCalendarFilter.class);

  static final String DEFAULT_COLOR = "#FAAF26";

  private static final Set<Integer> EMPTY_INT_SET = new HashSet<Integer>();

  private final List<TemplateEntry> templateEntries;

  private int activeTemplateEntryIndex = 0;

  private transient TemplateEntry activeTemplateEntry = null;

  public TeamCalCalendarFilter()
  {
    super();
    templateEntries = new ArrayList<TemplateEntry>();
  }

  /**
   * Try to find a previous used color for the given calendar in any entry of this filter. If found multiple ones, the newest one is used.
   * @param calId Id of the calendar to search for.
   * @return Previous used color for the given calendar or DEFAULT_COLOR if not found.
   */
  public String getUsedColor(final Integer calId)
  {
    String lastCalendarColor = null;
    String lastColor = DEFAULT_COLOR;
    long lastCalendarEntry = 0;
    long lastEntry = 0;
    if (calId == null) {
      return lastCalendarColor;
    }
    // intelligent color choose
    for (final TemplateEntry entry : templateEntries) {
      for (final TemplateCalendarProperties props : entry.getCalendarProperties()) {
        if (calId.equals(props.getCalId()) == true) {
          if (props.getMillisOfLastChange() > lastCalendarEntry) {
            lastCalendarEntry = props.getMillisOfLastChange();
            lastCalendarColor = props.getColorCode();
          }
        }
        if (props.getMillisOfLastChange() > lastEntry) {
          lastEntry = props.getMillisOfLastChange();
          lastColor = props.getColorCode();
        }
      }
    }
    return lastCalendarColor != null ? lastCalendarColor : lastColor;
  }

  /**
   * @return the templateEntries
   */
  public List<TemplateEntry> getTemplateEntries()
  {
    if (templateEntries.size() == 0) {
      createDefaultEntry();
    }
    return templateEntries;
  }

  /**
   * Adds new entry and sets the new entry as active entry.
   * @param entry
   */
  public void add(final TemplateEntry entry)
  {
    synchronized (templateEntries) {
      templateEntries.add(entry);
      Collections.sort(templateEntries);
      setActiveTemplateEntry(entry);
    }
  }

  public void remove(final TemplateEntry entry)
  {
    synchronized (templateEntries) {
      final int index = templateEntries.indexOf(entry);
      templateEntries.remove(entry);
      if (index == activeTemplateEntryIndex) {
        activeTemplateEntryIndex = 0;
      }
      if (templateEntries.size() == 0) {
        createDefaultEntry();
      }
    }
    activeTemplateEntry = null;
  }

  /**
   * @return the activeTemplateEntryIndex
   */
  public int getActiveTemplateEntryIndex()
  {
    return activeTemplateEntryIndex;
  }

  /**
   * @param activeTemplateEntryIndex the activeTemplateEntryIndex to set
   * @return this for chaining.
   */
  public TeamCalCalendarFilter setActiveTemplateEntryIndex(final int activeTemplateEntryIndex)
  {
    this.activeTemplateEntryIndex = activeTemplateEntryIndex;
    // Force to get active template entry by new index:
    this.activeTemplateEntry = null;
    return this;
  }

  /**
   * @return the activeTemplateEntry
   */
  public TemplateEntry getActiveTemplateEntry()
  {
    if (this.activeTemplateEntry == null) {
      if (this.activeTemplateEntryIndex >= 0 && this.activeTemplateEntryIndex < this.templateEntries.size()) {
        this.activeTemplateEntry = this.templateEntries.get(this.activeTemplateEntryIndex);
        this.activeTemplateEntry.setDirty();
      } else {
        // No filter entry given, create the standard filter:
        createDefaultEntry();
      }
    }
    return this.activeTemplateEntry;
  }

  /**
   * @param activeTemplateEntry the activeTemplateEntry to set
   * @return this for chaining.
   */
  public TeamCalCalendarFilter setActiveTemplateEntry(final TemplateEntry activeTemplateEntry)
  {
    int i = 0;
    for (final TemplateEntry entry : this.templateEntries) {
      if (entry.equals(activeTemplateEntry) == true) {
        this.activeTemplateEntryIndex = i;
        this.activeTemplateEntry = entry;
        this.activeTemplateEntry.setDirty();
        break;
      }
      i++;
    }
    return this;
  }

  public Set<Integer> getActiveVisibleCalendarIds()
  {
    if (getActiveTemplateEntry() != null) {
      return this.activeTemplateEntry.getVisibleCalendarIds();
    } else {
      if (EMPTY_INT_SET.isEmpty() == false) {
        log.error("************** Oups, dear developers, don't add entries to the empty HashSet returned by this method!!!!");
        EMPTY_INT_SET.clear();
      }
      return EMPTY_INT_SET;
    }
  }

  /**
   * Copies all template entries (active template and list) of the given source to this. This method is used to make a backup copy for
   * undoing changes in TeamCalDialog.
   * @param src
   */
  public TeamCalCalendarFilter copyValuesFrom(final TeamCalCalendarFilter src)
  {
    if (src == null) {
      return this;
    }
    this.templateEntries.clear();
    for (final TemplateEntry srcEntry : src.templateEntries) {
      final TemplateEntry entry = srcEntry.clone();
      this.templateEntries.add(entry);
    }
    this.activeTemplateEntryIndex = src.activeTemplateEntryIndex;
    this.activeTemplateEntry = null;
    return this;
  }

  /**
   * For avoiding reload of Calendar if no changes are detected. (Was für'n Aufwand für so'n kleines Feature...)
   * @param other
   * @return
   */
  public boolean isModified(final TeamCalCalendarFilter other)
  {
    if (other == null) {
      return false;
    }
    if (this.activeTemplateEntryIndex != other.activeTemplateEntryIndex) {
      return true;
    }
    if (templateEntries.size() != other.templateEntries.size()) {
      return true;
    }
    final Iterator<TemplateEntry> it1 = this.templateEntries.iterator();
    final Iterator<TemplateEntry> it2 = other.templateEntries.iterator();
    while (it1.hasNext() == true) {
      final TemplateEntry entry1 = it1.next();
      final TemplateEntry entry2 = it2.next();
      if (entry1.isModified(entry2) == true) {
        return true;
      }
    }
    return false;
  }

  public String getNewTemplateName(final String prefix)
  {
    if (templateEntries == null) {
      return prefix;
    }
    String current = prefix;
    for (int i = 1; i <= 10; i++) {
      for (final TemplateEntry entry : templateEntries) {
        if (current.equals(entry.getName()) == true) {
          if (i == 10) {
            // Don't try to get prefix + " 11", giving up:
            return null;
          }
          current = prefix + " " + i;
          break;
        }
      }
    }
    return current;
  }

  private void createDefaultEntry()
  {
    final TemplateEntry newTemplate = new TemplateEntry();
    newTemplate.setName(PFUserContext.getLocalizedString("default"));
    newTemplate.setTimesheetUserId(PFUserContext.getUserId()).setShowBirthdays(true).setShowBreaks(true).setShowPlanning(true)
    .setShowStatistics(true);
    add(newTemplate);
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#isShowBirthdays()
   */
  @Override
  public boolean isShowBirthdays()
  {
    if (getActiveTemplateEntry() == null) {
      return false;
    }
    return activeTemplateEntry.isShowBirthdays();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setShowBirthdays(boolean)
   */
  @Override
  public TeamCalCalendarFilter setShowBirthdays(final boolean showBirthdays)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setShowBirthdays(showBirthdays);
    }
    return this;
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#isShowStatistics()
   */
  @Override
  public boolean isShowStatistics()
  {
    if (getActiveTemplateEntry() == null) {
      return false;
    }
    return activeTemplateEntry.isShowStatistics();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setShowStatistics(boolean)
   */
  @Override
  public TeamCalCalendarFilter setShowStatistics(final boolean showStatistics)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setShowStatistics(showStatistics);
    }
    return this;
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#isShowPlanning()
   */
  @Override
  public boolean isShowPlanning()
  {
    if (getActiveTemplateEntry() == null) {
      return false;
    }
    return activeTemplateEntry.isShowPlanning();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setShowPlanning(boolean)
   */
  @Override
  public TeamCalCalendarFilter setShowPlanning(final boolean showPlanning)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setShowPlanning(showPlanning);
    }
    return this;
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#getTimesheetUserId()
   */
  @Override
  public Integer getTimesheetUserId()
  {
    if (getActiveTemplateEntry() == null) {
      return null;
    }
    return activeTemplateEntry.getTimesheetUserId();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setTimesheetUserId(java.lang.Integer)
   */
  @Override
  public TeamCalCalendarFilter setTimesheetUserId(final Integer timesheetUserId)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setTimesheetUserId(timesheetUserId);
    }
    return this;
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#isShowTimesheets()
   */
  @Override
  public boolean isShowTimesheets()
  {
    if (getActiveTemplateEntry() == null) {
      return false;
    }
    return activeTemplateEntry.isShowTimesheets();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setShowTimesheets(boolean)
   */
  @Override
  public TeamCalCalendarFilter setShowTimesheets(final boolean showTimesheets)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setShowTimesheets(showTimesheets);
    }
    return this;
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#isShowBreaks()
   */
  @Override
  public boolean isShowBreaks()
  {
    if (getActiveTemplateEntry() == null) {
      return false;
    }
    return activeTemplateEntry.isShowBreaks();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setShowBreaks(boolean)
   */
  @Override
  public TeamCalCalendarFilter setShowBreaks(final boolean showBreaks)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setShowBreaks(showBreaks);
    }
    return this;
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#getSelectedCalendar()
   */
  @Override
  public String getSelectedCalendar()
  {
    if (getActiveTemplateEntry() == null) {
      return null;
    }
    return activeTemplateEntry.getSelectedCalendar();
  }

  /**
   * @see org.projectforge.web.calendar.ICalendarFilter#setSelectedCalendar(java.lang.String)
   */
  @Override
  public TeamCalCalendarFilter setSelectedCalendar(final String selectedCalendar)
  {
    if (getActiveTemplateEntry() != null) {
      activeTemplateEntry.setSelectedCalendar(selectedCalendar);
    }
    return this;
  }
}
