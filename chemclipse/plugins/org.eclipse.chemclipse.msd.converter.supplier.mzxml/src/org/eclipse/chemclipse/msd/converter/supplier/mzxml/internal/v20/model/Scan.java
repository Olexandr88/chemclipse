/*******************************************************************************
 * Copyright (c) 2015, 2024 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.mzxml.internal.v20.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"scansIntegration", "precursorMz", "maldi", "peaks", "nameValueAndComment", "scan"})
@XmlRootElement(name = "scan")
public class Scan implements Serializable {

	private static final long serialVersionUID = 200L;
	private List<ScansIntegration> scansIntegration;
	private List<PrecursorMz> precursorMz;
	private Maldi maldi;
	@XmlElement(required = true, nillable = true)
	private Peaks peaks;
	@XmlElements({@XmlElement(name = "nameValue", type = NameValue.class), @XmlElement(name = "comment", type = String.class)})
	private List<Serializable> nameValueAndComment;
	private List<Scan> scan;
	@XmlAttribute(name = "num", required = true)
	@XmlSchemaType(name = "positiveInteger")
	private BigInteger num;
	@XmlAttribute(name = "msLevel", required = true)
	@XmlSchemaType(name = "positiveInteger")
	private BigInteger msLevel;
	@XmlAttribute(name = "peaksCount", required = true)
	@XmlSchemaType(name = "nonNegativeInteger")
	private BigInteger peaksCount;
	@XmlAttribute(name = "polarity")
	private String polarity;
	@XmlAttribute(name = "scanType")
	private String scanType;
	@XmlAttribute(name = "centroided")
	private Boolean centroided;
	@XmlAttribute(name = "deisotoped")
	private Boolean deisotoped;
	@XmlAttribute(name = "chargeDeconvoluted")
	private Boolean chargeDeconvoluted;
	@XmlAttribute(name = "retentionTime")
	private Duration retentionTime;
	@XmlAttribute(name = "ionisationEnergy")
	private Float ionisationEnergy;
	@XmlAttribute(name = "collisionEnergy")
	private Float collisionEnergy;
	@XmlAttribute(name = "startMz")
	private Float startMz;
	@XmlAttribute(name = "endMz")
	private Float endMz;
	@XmlAttribute(name = "lowMz")
	private Float lowMz;
	@XmlAttribute(name = "highMz")
	private Float highMz;
	@XmlAttribute(name = "basePeakMz")
	private Float basePeakMz;
	@XmlAttribute(name = "basePeakIntensity")
	private Float basePeakIntensity;
	@XmlAttribute(name = "totIonCurrent")
	private Float totIonCurrent;

	public List<ScansIntegration> getScansIntegration() {

		if(scansIntegration == null) {
			scansIntegration = new ArrayList<ScansIntegration>();
		}
		return this.scansIntegration;
	}

	public List<PrecursorMz> getPrecursorMz() {

		if(precursorMz == null) {
			precursorMz = new ArrayList<PrecursorMz>();
		}
		return this.precursorMz;
	}

	public Maldi getMaldi() {

		return maldi;
	}

	public void setMaldi(Maldi value) {

		this.maldi = value;
	}

	public Peaks getPeaks() {

		return peaks;
	}

	public void setPeaks(Peaks value) {

		this.peaks = value;
	}

	public List<Serializable> getNameValueAndComment() {

		if(nameValueAndComment == null) {
			nameValueAndComment = new ArrayList<Serializable>();
		}
		return this.nameValueAndComment;
	}

	public List<Scan> getScan() {

		if(scan == null) {
			scan = new ArrayList<Scan>();
		}
		return this.scan;
	}

	public BigInteger getNum() {

		return num;
	}

	public void setNum(BigInteger value) {

		this.num = value;
	}

	public BigInteger getMsLevel() {

		return msLevel;
	}

	public void setMsLevel(BigInteger value) {

		this.msLevel = value;
	}

	public BigInteger getPeaksCount() {

		return peaksCount;
	}

	public void setPeaksCount(BigInteger value) {

		this.peaksCount = value;
	}

	public String getPolarity() {

		return polarity;
	}

	public void setPolarity(String value) {

		this.polarity = value;
	}

	public String getScanType() {

		return scanType;
	}

	public void setScanType(String value) {

		this.scanType = value;
	}

	public Boolean isCentroided() {

		return centroided;
	}

	public void setCentroided(Boolean value) {

		this.centroided = value;
	}

	public Boolean isDeisotoped() {

		return deisotoped;
	}

	public void setDeisotoped(Boolean value) {

		this.deisotoped = value;
	}

	public boolean isChargeDeconvoluted() {

		if(chargeDeconvoluted == null) {
			return false;
		} else {
			return chargeDeconvoluted;
		}
	}

	public void setChargeDeconvoluted(Boolean value) {

		this.chargeDeconvoluted = value;
	}

	public Duration getRetentionTime() {

		return retentionTime;
	}

	public void setRetentionTime(Duration value) {

		this.retentionTime = value;
	}

	public Float getIonisationEnergy() {

		return ionisationEnergy;
	}

	public void setIonisationEnergy(Float value) {

		this.ionisationEnergy = value;
	}

	public Float getCollisionEnergy() {

		return collisionEnergy;
	}

	public void setCollisionEnergy(Float value) {

		this.collisionEnergy = value;
	}

	public Float getStartMz() {

		return startMz;
	}

	public void setStartMz(Float value) {

		this.startMz = value;
	}

	public Float getEndMz() {

		return endMz;
	}

	public void setEndMz(Float value) {

		this.endMz = value;
	}

	public Float getLowMz() {

		return lowMz;
	}

	public void setLowMz(Float value) {

		this.lowMz = value;
	}

	public Float getHighMz() {

		return highMz;
	}

	public void setHighMz(Float value) {

		this.highMz = value;
	}

	public Float getBasePeakMz() {

		return basePeakMz;
	}

	public void setBasePeakMz(Float value) {

		this.basePeakMz = value;
	}

	public Float getBasePeakIntensity() {

		return basePeakIntensity;
	}

	public void setBasePeakIntensity(Float value) {

		this.basePeakIntensity = value;
	}

	public Float getTotIonCurrent() {

		return totIonCurrent;
	}

	public void setTotIonCurrent(Float value) {

		this.totIonCurrent = value;
	}
}
