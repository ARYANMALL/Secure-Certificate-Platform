import { useEffect, useState } from "react";

const API = "http://localhost:8080";

/* =========================================================
   API HELPER
   ========================================================= */

async function apiJson(url, options = {}) {
  const response = await fetch(url, options);

  if (!response.ok) {
    let message = `Request failed (${response.status})`;

    try {
      const errorData = await response.json();

      message =
        errorData.message ||
        errorData.error ||
        message;
    } catch {
      // Server did not return JSON.
    }

    throw new Error(message);
  }

  try {
    return await response.json();
  } catch {
    return {};
  }
}

/* =========================================================
   CENTRAL ISSUER - ISSUE CERTIFICATE
   ========================================================= */

export default function IssuerIssueCertificate({
  token
}) {
  const [institutions, setInstitutions] =
    useState([]);

  const [students, setStudents] =
    useState([]);

  const [institutionId, setInstitutionId] =
    useState("");

  const [studentId, setStudentId] =
    useState("");

  const [courseName, setCourseName] =
    useState("");

  const [description, setDescription] =
    useState("");

  const [loadingInstitutions, setLoadingInstitutions] =
    useState(true);

  const [loadingStudents, setLoadingStudents] =
    useState(false);

  const [submitting, setSubmitting] =
    useState(false);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  /* =======================================================
     LOAD ALL ACTIVE INSTITUTIONS
     ======================================================= */

  useEffect(() => {
    let cancelled = false;

    async function loadInstitutions() {
      try {
        const data = await apiJson(
          `${API}/api/issuer/manage/institutions`,
          {
            headers: {
              Authorization:
                `Bearer ${token}`
            }
          }
        );

        if (!cancelled) {
          setInstitutions(
            Array.isArray(data)
              ? data
              : []
          );
        }

      } catch (err) {

        if (!cancelled) {
          setError(err.message);
        }

      } finally {

        if (!cancelled) {
          setLoadingInstitutions(false);
        }
      }
    }

    loadInstitutions();

    return () => {
      cancelled = true;
    };
  }, [token]);

  /* =======================================================
     LOAD STUDENTS FOR SELECTED INSTITUTION
     ======================================================= */

  useEffect(() => {
    if (!institutionId) {
      return undefined;
    }

    let cancelled = false;

    async function loadStudents() {
      try {
        const data = await apiJson(
          `${API}/api/issuer/manage/students?institutionId=${encodeURIComponent(
            institutionId
          )}`,
          {
            headers: {
              Authorization:
                `Bearer ${token}`
            }
          }
        );

        if (!cancelled) {
          setStudents(
            Array.isArray(data)
              ? data
              : []
          );

          setStudentId("");
        }

      } catch (err) {

        if (!cancelled) {
          setStudents([]);
          setStudentId("");
          setError(err.message);
        }

      } finally {

        if (!cancelled) {
          setLoadingStudents(false);
        }
      }
    }

    loadStudents();

    return () => {
      cancelled = true;
    };
  }, [institutionId, token]);

  /* =======================================================
     INSTITUTION CHANGE
     ======================================================= */

  function handleInstitutionChange(event) {
    const selectedId =
      event.target.value;

    setInstitutionId(selectedId);
    setStudentId("");
    setStudents([]);

    setError("");
    setMessage("");

    if (selectedId) {
      setLoadingStudents(true);
    } else {
      setLoadingStudents(false);
    }
  }

  /* =======================================================
     SUBMIT CERTIFICATE FOR ADMIN APPROVAL
     ======================================================= */

  async function submit(event) {
    event.preventDefault();

    setError("");
    setMessage("");

    if (!institutionId) {
      setError(
        "Please select an institution."
      );
      return;
    }

    if (!studentId) {
      setError(
        "Please select a student."
      );
      return;
    }

    if (!courseName.trim()) {
      setError(
        "Course or program is required."
      );
      return;
    }

    setSubmitting(true);

    try {

      await apiJson(
        `${API}/api/issuer/manage/certificate-requests`,
        {
          method: "POST",

          headers: {
            Authorization:
              `Bearer ${token}`,

            "Content-Type":
              "application/json"
          },

          body: JSON.stringify({
            studentId,
            courseName:
              courseName.trim(),
            description:
              description.trim()
          })
        }
      );

      setMessage(
        "Certificate request submitted to the administrator for approval."
      );

      setStudentId("");
      setCourseName("");
      setDescription("");

    } catch (err) {

      setError(err.message);

    } finally {

      setSubmitting(false);
    }
  }

  /* =======================================================
     UI
     ======================================================= */

  return (
    <div className="content">

      {/* ===================================================
          ISSUE CERTIFICATE
          =================================================== */}

      <section className="card">

        <span className="eyebrow">
          CENTRAL ISSUER
        </span>

        <h3>
          Issue a Certificate
        </h3>

        <p>
          Prepare a certificate for any active
          student from any active institution.
          Administrator approval is required
          before the certificate becomes active.
        </p>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {message && (
          <div className="success">
            {message}
          </div>
        )}

        <form
          className="form"
          onSubmit={submit}
        >

          {/* INSTITUTION */}

          <label>
            Institution
          </label>

          <select
            value={institutionId}
            onChange={handleInstitutionChange}
            disabled={loadingInstitutions}
            required
          >

            <option value="">
              {loadingInstitutions
                ? "Loading institutions..."
                : "Select institution"}
            </option>

            {institutions.map(
              (institution) => (

                <option
                  key={institution.id}
                  value={institution.id}
                >
                  {institution.name}
                </option>

              )
            )}

          </select>

          {/* STUDENT */}

          <label>
            Student
          </label>

          <select
            value={studentId}
            onChange={(event) =>
              setStudentId(
                event.target.value
              )
            }
            disabled={
              !institutionId ||
              loadingStudents
            }
            required
          >

            <option value="">

              {!institutionId
                ? "Select institution first"
                : loadingStudents
                  ? "Loading students..."
                  : students.length === 0
                    ? "No active students found"
                    : "Select student"}

            </option>

            {students.map(
              (student) => (

                <option
                  key={student.id}
                  value={student.id}
                >
                  {student.fullName}
                  {" - "}
                  {student.email}
                </option>

              )
            )}

          </select>

          {/* COURSE */}

          <label>
            Course / Program / Field
          </label>

          <input
            type="text"
            value={courseName}
            onChange={(event) =>
              setCourseName(
                event.target.value
              )
            }
            placeholder="Data Science"
            maxLength={200}
            required
          />

          {/* DESCRIPTION */}

          <label>
            Description
          </label>

          <textarea
            rows="6"
            value={description}
            onChange={(event) =>
              setDescription(
                event.target.value
              )
            }
            placeholder="Describe the qualification or achievement."
            maxLength={2000}
          />

          {/* SUBMIT */}

          <button
            type="submit"
            className="primary"
            disabled={
              submitting ||
              !institutionId ||
              !studentId
            }
          >
            {submitting
              ? "Submitting for approval..."
              : "Submit for Admin Approval"}
          </button>

        </form>

      </section>

      {/* ===================================================
          WORKFLOW
          =================================================== */}

      <section className="card">

        <span className="eyebrow">
          CERTIFICATE WORKFLOW
        </span>

        <h3>
          Central issuer process
        </h3>

        <p>
          Amit can prepare certificates for
          students from any approved institution.
        </p>

        <div className="flow">

          <span>
            Institution
          </span>

          <b>→</b>

          <span>
            Student
          </span>

          <b>→</b>

          <span>
            Central Issuer
          </span>

          <b>→</b>

          <span>
            Admin
          </span>

          <b>→</b>

          <span>
            SHA-256
          </span>

          <b>→</b>

          <span>
            RSA
          </span>

        </div>

      </section>

    </div>
  );
}