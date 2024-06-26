import React, {useEffect, useState} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import Select from 'react-select'
import DatePicker from 'react-datepicker'
import {Prompt, useHistory, useParams} from "react-router-dom";
import {
    performDeletePMCReport,
    performReplacePMCReport, performRevertPMCReportToDraft,
    performSubmitPMCReport,
    selectPMCReports
} from "./pmcReportsSlice";
import { selectMetadata, selectTendersById } from "./metadataSlice";
import {DP_DATE_FORMAT, PMCReportStatus} from "../../app/constants";

import "react-datepicker/dist/react-datepicker.css";
import "./../../react-datepicker.css";
import {formatDateForAPI, parseDate} from "../../app/date";
import {Border} from "./Border";

const scrollToFirstError = () => {
    let elems = document.getElementsByClassName("is-invalid");
    if (elems && elems.length) {
        window.scrollTo(0, elems[0].parentElement.offsetTop);
    }
}

export function EditReport(props) {
    const pmcReports = useSelector(selectPMCReports);
    const metadata = useSelector(selectMetadata);
    const subCounties = metadata.ref["Subcounty"];
    const fiscalYears = metadata.ref["FiscalYear"];
    const departments = metadata.ref["Department"];
    const tenders = metadata.ref["Tender"];
    const tendersById = selectTendersById(metadata);
    const wards = metadata.ref["Ward"];
    const pmcStaff = metadata.ref["PMCStaff"];
    const pmcStatus = metadata.ref["PMCStatus"];
    const designations = metadata.ref["Designation"];
    const projectClosureHandoverOptions = metadata.ref["ProjectClosureHandover"];

    const [isBlocking, setIsBlocking] = useState(false);

    const filterWards = (subCountyIds, wards) => {
        return (wards || []).filter(w => (subCountyIds || []).some(scId => w.parentId === scId));
    };

    function filterTenders(fyId, deptId, tenders) {
        if (!deptId || !fyId) {
            return [];
        }
        return (tenders || []).filter(t => t.fyId === fyId && t.deptId === deptId);
    }

    const { internalId } = useParams();
    const originalReport = pmcReports.reports[internalId];

    const tender = tendersById[(originalReport || {}).tenderId] || {};

    const status = isNaN(internalId) ? PMCReportStatus.DRAFT : (originalReport || {}).status;
    const isDisabled = status !== PMCReportStatus.DRAFT;

    const [report, setReport] = useState({
        authorizePayment: false,
        acknowledgeSignature: false,
        fyId: tender.fyId,
        deptId: tender.deptId,
        status: status,
        ...originalReport
    });

    const [errors, setErrors] = useState({});
    const [submit, setSubmit] = useState(false);

    const availableWards = filterWards(report.subcountyIds, wards);
    const availableTenders = filterTenders(report.fyId, report.deptId, tenders);

    const validate = (report) => {
        let errors = {};
        let valid = true;

        const notNull = (errors, obj, field, message) => {
            if (!obj[field]) {
                errors[field] = message;
                valid = false;
            }
        };
        const nonEmptyArray = (errors, obj, field, message) => {
            const array = obj[field];
            if (!array || !array.length) {
                errors[field] = message;
                valid = false;
            }
        }

        notNull(errors, report, 'fyId', 'Required');
        notNull(errors, report, 'deptId', 'Required');
        notNull(errors, report, 'tenderId', 'Required');
        notNull(errors, report, 'reportDate', 'Required');
        nonEmptyArray(errors, report, 'subcountyIds', 'Please select at least one subcounty');
        nonEmptyArray(errors, report, 'wardIds', 'Please select at least one ward');

        nonEmptyArray(errors, report, 'pmcMembers', 'Please add at least one PMC member');
        if (report.pmcMembers && report.pmcMembers.length) {
            errors.pmcMembersArray = report.pmcMembers.map(member => {
                let memberErrors = {};
                notNull(memberErrors, member, 'staffId', 'Required');
                notNull(memberErrors, member, 'designationId', 'Required');
                return memberErrors;
            });
        }

        notNull(errors, report, 'socialSafeguards', 'Required');
        notNull(errors, report, 'emergingComplaints', 'Required');
        notNull(errors, report, 'pmcChallenges', 'Required');

        if (report.pmcNotes && report.pmcNotes.length) {
            errors.pmcNotesArray = report.pmcNotes.map(note => {
                let noteErrors = {};
                notNull(noteErrors, note, 'text', 'Required');
                return noteErrors;
            });
        }

        notNull(errors, report, 'pmcStatusId', 'Required');

        if (report.acknowledgeSignature !== true) {
            errors.acknowledgeSignature = 'Report must be signed';
            valid = false;
        }
        notNull(errors, report, 'signatureNames', 'Required');

        return [valid, errors];
    };

    const fieldChanged = e => {
        const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
        const newReport = {
            ...report,
            [e.target.name]: value
        };
        setReport(newReport);
        setErrors({
            ...errors,
            [e.target.name]: undefined,
            [e.target.name + 'Array']: undefined
        })
        setIsBlocking(true);

        if (submit) {
            const [, errors] = validate(newReport);
            setErrors(errors);
        }
    };

    useEffect(() => {
        setReport(report => {
            let ids = {};
            filterWards(report.subcountyIds, wards).forEach(w => ids[w.id] = true);
            return {
                ...report,
                wardIds: (report.wardIds || []).filter(wardId => ids[wardId])
            }
        });
    }, [report.subcountyIds, wards]);

    useEffect(() => {
        setReport(report => {
            const filteredTenders = filterTenders(report.fyId, report.deptId,
                report.tenderId ? [ tendersById[report.tenderId] ] : []);
            return {
                ...report,
                tenderId: filteredTenders.length === 0 ? null : filteredTenders[0].id
            }
        });
    }, [report.fyId, report.deptId, tendersById]);

    const history = useHistory();

    const handleCancel = (e) => {
        e.preventDefault();
        history.goBack();
    };

    const dispatch = useDispatch();

    const removeExtraFields = report => {
        let newReport = {...report};
        delete newReport.fyId;
        delete newReport.deptId;
        delete newReport.syncError;
        return newReport;
    }

    const handleSave = (e) => {
        e.preventDefault();

        dispatch(performReplacePMCReport(removeExtraFields(report)));
        setIsBlocking(false);
        history.goBack();
    };

    useEffect(() => {
        if (submit) {
            scrollToFirstError();
        }
        setSubmit(false);
    }, [submit]);

    const handleSubmit = e => {
        e.preventDefault();

        const [valid, errors] = validate(report);
        setErrors(errors);
        setSubmit(true);

        if (valid) {
            dispatch(performSubmitPMCReport(removeExtraFields(report)));
            setIsBlocking(false);
            history.goBack();
        }
    };

    const handleDelete = e => {
        e.preventDefault();
        setIsBlocking(false);
        dispatch(performDeletePMCReport(report.internalId));
        history.goBack();
    };

    const handleRevertToDraft = e => {
        e.preventDefault();
        dispatch(performRevertPMCReportToDraft(report.internalId));
        history.goBack();
    };

    return (
        <Border title={isNaN(internalId) ? "New report" : "Edit report"}>
        <div className="container-fluid pt-3 pb-3">
            <Prompt
                when={isBlocking}
                message='Your changes will be lost. Are you sure?' />

            <SelectCategoryField label="Fiscal Year" name="fyId" value={report.fyId} options={fiscalYears}
                                 onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <SelectCategoryField label="Department" name="deptId" value={report.deptId} options={departments}
                                 onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <SelectCategoryField label="Tender Title" name="tenderId" value={report.tenderId} options={availableTenders}
                                 onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <DateField label="Report Date" name="reportDate" value={report.reportDate} onChange={fieldChanged}
                       errors={errors} isDisabled={isDisabled} />

            <SelectCategoryField label="Subcounties" name="subcountyIds" value={report.subcountyIds} options={subCounties}
                                 isMulti onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <SelectCategoryField label="Wards" name="wardIds" value={report.wardIds} options={availableWards}
                                 isMulti onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <PMCMembers name="pmcMembers" value={report.pmcMembers} pmcStaff={pmcStaff} designations={designations}
                        onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <Notes name="pmcNotes" value={report.pmcNotes} onChange={fieldChanged} errors={errors}
                   isDisabled={isDisabled} />

            <TextAreaField label="Social safeguards" name="socialSafeguards" value={report.socialSafeguards}
                           onChange={fieldChanged} errors={errors} isDisabled={isDisabled}/>

            <TextAreaField label="Emerging complaints(GRM)" name="emergingComplaints" value={report.emergingComplaints}
                           onChange={fieldChanged} errors={errors} isDisabled={isDisabled}/>

            <TextAreaField label="PMC challenges" name="pmcChallenges" value={report.pmcChallenges}
                           onChange={fieldChanged} errors={errors} isDisabled={isDisabled}/>

            <SelectCategoryField label="CBM/PMC report" name="pmcStatusId"
                                 value={report.pmcStatusId} options={pmcStatus}
                                 onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <CheckboxField label="Authorize payment" name="authorizePayment" value={report.authorizePayment}
                           onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <SelectCategoryField label="Project Closure and Handover" name="projectClosureHandoverIds" isMulti
                                 value={report.projectClosureHandoverIds} options={projectClosureHandoverOptions}
                                 onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <CheckboxField label="eSignature" name="acknowledgeSignature" value={report.acknowledgeSignature}
                           onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <TextField label="eSignature First Name & Last Name" name="signatureNames" value={report.signatureNames}
                       onChange={fieldChanged} errors={errors} isDisabled={isDisabled} />

            <StatusComments statusComments={report.statusComments} />

            <div>
                {
                    report.status === PMCReportStatus.DRAFT &&
                    (<>
                        <button type="button" className="btn btn-primary" onClick={handleSave}>Save as Draft</button>
                        &nbsp;
                        <button type="button" className="btn btn-success" onClick={handleSubmit}>Submit</button>
                        &nbsp;
                    </>)
                }
                {
                    report.status === PMCReportStatus.SUBMITTED_PENDING &&
                    (<>
                        <button type="button" className="btn btn-primary" onClick={handleRevertToDraft}>Revert to Draft</button>
                        &nbsp;
                    </>)
                }
                <button type="button" className="btn btn-secondary" onClick={handleCancel}>Cancel</button>
                {
                    isNaN(report.id) && !isNaN(internalId) &&
                    (<>
                        &nbsp;
                        <button type="button" className="btn btn-danger" onClick={handleDelete}>Delete</button>
                    </>)
                }
            </div>
        </div>
        </Border>
    );
}

function StatusComments(props) {
    const statusComments = props.statusComments

    if (!statusComments || statusComments.length === 0) {
        return null
    }

    return (<>
        <h4>Status comments</h4>
        {
            statusComments.map((comment, idx) => {
                const d = new Date(comment.createdDate)
                const prettyDateAndTime = `${d.toLocaleDateString()} ${d.toLocaleTimeString()}`
                return (<p key={idx}>
                    <span className="text-muted">
                        Status changed to {comment.status} on {prettyDateAndTime} by {comment.createdBy}
                    </span>
                    <br/>{comment.comment}
                </p>)
            })
        }
    </>)
}

function PMCMembers(props) {
    const pmcMembers = props.value || [];

    const onChange = value => props.onChange({
        target: {
            name: props.name,
            value: value
        }
    });

    const changeField = (idx) => e => {
        const newValue = pmcMembers.map((m, sidx) => {
            return (sidx !== idx) ? m : {...m, [ e.target.name ]: e.target.value};
        })
        onChange(newValue);
    };

    const removePMCMember = (idx) => () => {
        const newValue = pmcMembers.filter((m, sidx) => sidx !== idx);
        onChange(newValue);
    };

    const addPMCMember = () => {
        const newValue = pmcMembers.concat({});
        onChange(newValue);
    };

    const error = (props.errors || {})[props.name];
    const pmcMemberErrors = (props.errors.pmcMembersArray || []);

    const pmcMemberRows = pmcMembers.map(
        (m, idx) => {
            const pmcMemberError = pmcMemberErrors[idx];
            return (
                <div key={idx} className="form-group">
                    <p>
                        Member #{idx + 1}
                        {props.isDisabled || (
                            <button type="button" className="btn btn-sm btn-danger float-right"
                                    onClick={removePMCMember(idx)}>
                                Remove PMC Member
                            </button>
                        )}
                    </p>

                    <SelectCategoryField label="Staff" name="staffId"
                                         value={m.staffId} options={props.pmcStaff} errors={pmcMemberError}
                                         onChange={changeField(idx)} isDisabled={props.isDisabled} />

                    <SelectCategoryField label="Designation" name="designationId"
                                         value={m.designationId} options={props.designations} errors={pmcMemberError}
                                         onChange={changeField(idx)} isDisabled={props.isDisabled} />
                </div>
            );
        }
    );

    return (
        <div className="form-group">
            <label>PMC Members</label>

            <div className={(error ? "is-invalid" : "")}>
                {pmcMemberRows}
            </div>

            {error && <div className="list-input invalid-feedback">{error}</div>}

            {props.isDisabled || (
                <div>
                    <button type="button" className="btn btn-success" onClick={addPMCMember}>Add PMC Member</button>
                </div>
            )}
        </div>
    );
}

function Notes(props) {
    const notes = props.value || [];

    const onChange = value => props.onChange({
        target: {
            name: props.name,
            value: value
        }
    });

    const changeField = (idx) => e => {
        const newValue = notes.map((note, sidx) => {
            return (sidx !== idx) ? note : {...note, [ e.target.name ]: e.target.value};
        })
        onChange(newValue);
    };

    const error = (props.errors || {})[props.name];
    const noteErrors = (props.errors.pmcNotesArray || []);

    const rows = notes.map(
        (note, idx) => {
            const textError = (noteErrors[idx] || {})["text"];

            return (
            <div key={idx} className="form-group">
                <p>
                    Note #{idx+1}
                    {props.isDisabled || (
                        <button type="button" className="btn btn-sm btn-danger float-right"
                                onClick={() => onChange(notes.filter((m, sidx) => sidx !== idx))}>
                            Remove Note
                        </button>
                    )}
                </p>

                <textarea name="text" value={note.text} onChange={changeField(idx)} rows="2"
                          className={"form-control" + (textError ? " is-invalid" : "")} disabled={props.isDisabled} />

                {textError && <div className="invalid-feedback">{textError}</div>}
            </div>
        )}
    );

    return (
        <div className="form-group">
            <label>Notes</label>

            <div className={(error ? "is-invalid" : "")}>
                {rows}
            </div>

            {error && <div className="list-input invalid-feedback">{error}</div>}

            {props.isDisabled || (
                <div>
                    <button type="button" className="btn btn-success"
                            onClick={() => onChange(notes.concat({}))}>
                        Add Note
                    </button>
                </div>
            )}
        </div>
    );

}

function SelectCategoryField(props) {
    const onChange = objVal => {
        let idVal;
        if (props.isMulti) {
            idVal = (objVal || []).map(el => el.id);
        } else {
            idVal = (objVal || {}).id;
        }
        const event = {
            target: {
                name: props.name,
                value: idVal
            }
        };
        props.onChange(event);
    }

    const options = props.options || [];

    let objValue;
    if (props.isMulti) {
        objValue = (props.value || []).map(valueId => ({
            id: valueId,
            label: (options.find(option => valueId === option.id) || {}).label
        }));
    } else {
        objValue = {
            id: props.value,
            label: (options.find(option => props.value === option.id) || {}).label
        };
    }

    const error = (props.errors || {})[props.name];

    const customStyles = {
        control: (provided, state) => ({
            ...provided,
            borderColor: error ? 'red' : provided.borderColor
        })
    };

    return (
        <div className="form-group">
            <label>{props.label}</label>
            <Select value={objValue} styles={customStyles} options={options} isMulti={props.isMulti}
                    onChange={onChange} getOptionValue={option => option['id']}
                    className={(error ? "is-invalid" : "")} isDisabled={props.isDisabled}/>
            {error && <div className="invalid-feedback">{error}</div>}
        </div>
    );
}

function DateField(props) {
    const dateChanged = value => {
        props.onChange({
            target: {
                name: props.name,
                value: formatDateForAPI(value)
            }
        });
    };

    const error = (props.errors || {})[props.name];

    const date = parseDate(props.value)

    return (
        <div className="form-group">
            <label>{props.label}</label>
            <div className={(error ? " is-invalid" : "")}>
                <DatePicker selected={date} onChange={dateChanged}
                            customInput={
                                <input type="text" className={"form-control" + (error ? " is-invalid" : "")} />
                            }
                            dateFormat={DP_DATE_FORMAT} disabled={props.isDisabled} />
            </div>
            {error && <div className="invalid-feedback">{error}</div>}
        </div>
    );
}

function CheckboxField(props) {
    const id = props.name;
    const error = (props.errors || {})[props.name];
    return (
        <div className="form-group form-check">
            <input name={props.name} id={id} type="checkbox"
                   className={"form-check-input" + (error ? " is-invalid" : "")}
                   checked={props.value} onChange={props.onChange} disabled={props.isDisabled} />
            <label className="form-check-label" htmlFor={id}>{props.label}</label>
            {error && <div className="invalid-feedback">{error}</div>}
        </div>
    );
}

function TextField(props) {
    const error = (props.errors || {})[props.name];
    return (
        <div className="form-group">
            <label>{props.label}</label>
            <input name={props.name} type="text" className={"form-control" + (error ? " is-invalid" : "")}
                   value={props.value || ''} onChange={props.onChange} disabled={props.isDisabled} />
            {error && <div className="invalid-feedback">{error}</div>}
        </div>
    );
}

function TextAreaField(props) {
    const error = (props.errors || {})[props.name];
    return (
        <div className="form-group">
            <label>{props.label}</label>
            <textarea name={props.name} value={props.value || ''} onChange={props.onChange} rows="2"
                      className={"form-control" + (error ? " is-invalid" : "")} disabled={props.isDisabled} />
            {error && <div className="invalid-feedback">{error}</div>}
        </div>
    );
}
