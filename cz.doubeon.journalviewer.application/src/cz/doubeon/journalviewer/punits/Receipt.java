package cz.doubeon.journalviewer.punits;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "receipts")
@NamedQuery(name = "Receipt.fromCashDeskPeriod", query = "SELECT r FROM Receipt r WHERE r.cashDesk.id = :id AND r.date BETWEEN :from AND :to ORDER BY r.date, r.number")
public class Receipt extends AbstractBean {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id; 
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	private String number;
	
	@Column(scale = 2, precision = 10)
	private BigDecimal total;
	
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	@ManyToOne
	private CashDesk cashDesk; 
	
	@ManyToOne
	private JournalFile journalFile;
	
	@OneToMany(mappedBy="receipt", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@OrderBy("itemOrder")
	private List<ReceiptItem> receiptItems = new ArrayList<ReceiptItem>();
	
	public Receipt(){}
	
	public Receipt(CashDesk cashDesk, JournalFile journalFile){
		setCashDesk(cashDesk);
		setJournalFile(journalFile);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		pcs.firePropertyChange("id", this.id, 
				this.id = id);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		pcs.firePropertyChange("date", this.date, 
				this.date = date);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		pcs.firePropertyChange("number", this.number, 
				this.number = number);
	}

	public JournalFile getJournalFile() {
		return journalFile;
	}

	public void setJournalFile(JournalFile journalFile) {
		pcs.firePropertyChange("journalFile", this.journalFile, 
				this.journalFile = journalFile);
	}

	public List<ReceiptItem> getReceiptItems() {
		return receiptItems;
	}

	public void setReceiptItems(List<ReceiptItem> receiptItems) {
		pcs.firePropertyChange("receiptItems",this.receiptItems,
				this.receiptItems = receiptItems);
	}

	public CashDesk getCashDesk() {
		return cashDesk;
	}

	public void setCashDesk(CashDesk cashDesk) {
		pcs.firePropertyChange("cashDesk", this.cashDesk, 
				this.cashDesk = cashDesk);
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		pcs.firePropertyChange("total", this.total, 
				this.total = total);
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		pcs.firePropertyChange("paymentType",this.paymentType,
				this.paymentType = paymentType);
	}

	
	
}
