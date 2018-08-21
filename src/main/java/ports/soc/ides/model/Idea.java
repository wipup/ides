package ports.soc.ides.model;

import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import ports.soc.ides.model.constant.IdeaStatus;
import ports.soc.ides.model.constant.ProjectType;
import ports.soc.util.DateTimeUtils;

public class Idea extends DataModel {

	private static final long serialVersionUID = 6909455785589626382L;

	private long id;

	@Size(max = 100)
	private String title;

	@Size(max = 4000)
	private String aim;

	@Size(max = 4000)
	private String deliverables;

	private IdeaStatus status;

	private LocalDateTime timestamp;

	private LocalDateTime createTime;

	private Organisation organisation;

	@Size(max = 4000)
	private String question;

	@Size(max = 100)
	private String student;

	private ProjectType type;

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAim() {
		return aim;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getQuestion() {
		return question;
	}

	public String getStudent() {
		return student;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAim(String aim) {
		this.aim = aim;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public void setStudent(String student) {
		this.student = student;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public IdeaStatus getStatus() {
		return status;
	}

	public void setStatus(IdeaStatus status) {
		this.status = status;
	}

	public ProjectType getType() {
		return type;
	}

	public void setType(ProjectType type) {
		this.type = type;
	}

	public String getDeliverables() {
		return deliverables;
	}

	public void setDeliverables(String deliverables) {
		this.deliverables = deliverables;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String printDetail() {
		StringBuilder sb = new StringBuilder();
		sb.append("Idea [id=").append(id).append(", title=").append(title).append(", aim=").append(aim).append(", deliverables=").append(deliverables).append(", status=")
				.append(status).append(", timestamp=").append(timestamp).append("(").append(DateTimeUtils.formatDateTime(timestamp)).append(")").append(", createTime=")
				.append(createTime).append("(").append(DateTimeUtils.formatDateTime(createTime)).append(")").append(", organisation=").append(DataModel.printDetail(organisation))
				.append(", question=").append(question).append(", student=").append(student).append(", type=").append(type).append("]");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Idea [#" + id + ", title=" + title + "]";
	}
}
