package cz.doubeon.journalviewer.punits;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "cashdesks")
@NamedQuery(name = "CashDesk.findAll", query = "SELECT c FROM CashDesk c")
public class CashDesk extends AbstractBean{

	@Id
	private String id;

	private String description;

	private String csvFolder;

	@OneToMany(mappedBy="cashDesk")
	private final List<Receipt> receipts = new ArrayList<Receipt>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		pcs.firePropertyChange("id", this.id, 
				this.id = id);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		pcs.firePropertyChange("description",this.description,
				this.description = description);
	}

	public String getCsvFolder() {
		return csvFolder;
	}

	public void setCsvFolder(String csvFolder) {
		pcs.firePropertyChange("csvFolder",this.csvFolder,
				this.csvFolder = csvFolder);
	}


}
