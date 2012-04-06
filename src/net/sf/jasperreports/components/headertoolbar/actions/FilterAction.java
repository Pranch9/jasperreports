/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2011 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jasperreports.components.headertoolbar.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import net.sf.jasperreports.components.sort.FilterTypesEnum;
import net.sf.jasperreports.components.sort.actions.FilterCommand;
import net.sf.jasperreports.components.sort.actions.FilterData;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignDatasetRun;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.repo.JasperDesignCache;
import net.sf.jasperreports.web.actions.ActionException;
import net.sf.jasperreports.web.commands.CommandException;
import net.sf.jasperreports.web.commands.ResetInCacheCommand;

/**
 * @author Narcis Marcu (narcism@users.sourceforge.net)
 * @version $Id$
 */
public class FilterAction extends AbstractVerifiableTableAction {
	
	public FilterAction() {
	}

	public FilterData getFilterData() {
		return (FilterData) columnData;
	}

	public void setFilterData(FilterData filterData) {
		columnData = filterData;
	}

	public void performAction() throws ActionException {
		JRDesignDatasetRun datasetRun = (JRDesignDatasetRun)table.getDatasetRun();

		String datasetName = datasetRun.getDatasetName();
		
		JasperDesignCache cache = JasperDesignCache.getInstance(getJasperReportsContext(), getReportContext());
		
		JasperDesign jasperDesign = cache.getJasperDesign(targetUri);
		JRDesignDataset dataset = (JRDesignDataset)jasperDesign.getDatasetMap().get(datasetName);
		
		// execute command
		try {
			getCommandStack().execute(
				new ResetInCacheCommand(
					new FilterCommand(dataset, getFilterData()),
					getJasperReportsContext(),
					getReportContext(), 
					targetUri
					)
				);
		} catch (CommandException e) {
			throw new ActionException(e.getMessage());
		}
	}

	@Override
	public void verify() throws ActionException {
		FilterData fd = getFilterData();
		if (fd.getFieldValueStart() != null && fd.getFieldValueStart().length() > 0 && fd.getFilterType() != null) {
			FilterTypesEnum filterType = FilterTypesEnum.getByName(fd.getFilterType());
			
			if (filterType == FilterTypesEnum.DATE) {
				if (fd.getFilterPattern() == null || fd.getFilterPattern().length() == 0) {
					errors.addAndThrow("Invalid filter pattern!");
				} else {
					Locale locale = (Locale)getReportContext().getParameterValue(JRParameter.REPORT_LOCALE);
					if (locale == null) {
						locale = Locale.getDefault();
					}
					SimpleDateFormat sdf = new SimpleDateFormat(fd.getFilterPattern(), locale);
					try {
						sdf.parse(fd.getFieldValueStart());
					} catch (ParseException e) {
						errors.add("Invalid date: " + fd.getFieldValueStart());
					}
					if (fd.getFieldValueEnd() != null && fd.getFieldValueEnd().length() > 0) {
						try {
							sdf.parse(fd.getFieldValueEnd());
						} catch (ParseException e) {
							errors.add("Invalid date: " + fd.getFieldValueEnd());
						}
					}
				}
			}
		}
	}

}
