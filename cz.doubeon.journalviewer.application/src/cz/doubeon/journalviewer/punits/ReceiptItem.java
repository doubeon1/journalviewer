package cz.doubeon.journalviewer.punits;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "receipt_item")
public class ReceiptItem extends AbstractBean {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long Id;

	private String text;

	@Column(scale = 2, precision = 10)
	private BigDecimal total;

	@Column(scale = 2, precision = 10)
	private BigDecimal quantity;

	private Integer itemOrder;

	@ManyToOne
	private Receipt receipt;

	public Integer getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(Integer itemOrder) {
		pcs.firePropertyChange("itemOrder", this.itemOrder, 
				this.itemOrder = itemOrder);
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		pcs.firePropertyChange("Id", this.Id, 
				this.Id = id);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		pcs.firePropertyChange("text",this.text,
				this.text = text);
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		pcs.firePropertyChange("total",this.total, 
				this.total = total);
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		pcs.firePropertyChange("quantity", this.quantity, 
				this.quantity = quantity);
	}

	public Receipt getReceipt() {
		return receipt;
	}

	public void setReceipt(Receipt receipt) {
		pcs.firePropertyChange("receipt", this.receipt,
				this.receipt = receipt);
	}


}
