package cz.doubeon.journalviewer.punits;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "journal_files")
@NamedQuery(name = "JournalFile.cashDeskJournals", query = "SELECT j FROM JournalFile j JOIN j.cashDesk c WHERE c.id = :id")
public class JournalFile extends AbstractBean {
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	private String fileName;

	@ManyToOne
	private CashDesk cashDesk;

	public JournalFile(){}

	public JournalFile(String fileName, CashDesk cashDesk){
		setFileName(fileName);
		setCashDesk(cashDesk);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		pcs.firePropertyChange("id", this.id, 
				this.id = id);
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileNameUpper() {
		return fileName == null ? null : fileName.toUpperCase();
	}

	public void setFileName(String fileName) {
		pcs.firePropertyChange("fileName", this.fileName, 
				this.fileName = fileName);
	}

	public CashDesk getCashDesk() {
		return cashDesk;
	}

	public void setCashDesk(CashDesk cashDesk) {
		pcs.firePropertyChange("cashDesk", this.cashDesk,
				this.cashDesk = cashDesk);
	}

}
