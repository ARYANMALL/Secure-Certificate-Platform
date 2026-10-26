import { useEffect, useState } from "react";
import "./App.css";
import RegisterPage from "./RegisterPage";
import InstitutionRegisterPage from "./InstitutionRegisterPage";

const API = "http://localhost:8080";

function App() {
  const path = window.location.pathname;

  // IMPORTANT:
  // Hooks must always run before conditional returns.
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(
        localStorage.getItem("user") || "null"
      );
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(
    localStorage.getItem("token")
  );

  /*
   * PUBLIC STUDENT REGISTRATION
   */
  if (path === "/register") {
    return (
      <RegisterPage
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /*
   * PUBLIC INSTITUTION REGISTRATION
   */
  if (path === "/register/institution") {
    return (
      <InstitutionRegisterPage
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /*
   * PUBLIC CERTIFICATE VERIFICATION
   */
  if (path.startsWith("/verify/")) {
    const certificateNumber = decodeURIComponent(
      path.substring("/verify/".length)
    );

    return (
      <VerifyPage
        initialNumber={certificateNumber}
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /*
   * PUBLIC VERIFICATION PAGE
   */
  if (path === "/verify") {
    return (
      <VerifyPage
        initialNumber=""
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /*
   * LOGIN
   */
  if (!token || !user) {
    return (
      <LoginPage
        onLogin={(data) => {
          const loggedUser = {
            userId: data.userId,
            fullName: data.fullName,
            email: data.email,
            role: data.role
          };

          localStorage.setItem(
            "token",
            data.accessToken
          );

          localStorage.setItem(
            "user",
            JSON.stringify(loggedUser)
          );

          setToken(data.accessToken);
          setUser(loggedUser);
        }}
      />
    );
  }

  /*
   * AUTHENTICATED APPLICATION
   */
  return (
    <Dashboard
      user={user}
      token={token}
      onLogout={() => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");

        setToken(null);
        setUser(null);
      }}
    />
  );
}

/* =========================================================
   LOGIN
   ========================================================= */

function LoginPage({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function login(event) {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await fetch(
        `${API}/api/auth/login`,
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json"
          },

          body: JSON.stringify({
            email: email.trim(),
            password
          })
        }
      );

      let data = {};

      try {
        data = await response.json();
      } catch {
        data = {};
      }

      if (!response.ok) {
        throw new Error(
          data.message ||
          "Invalid email or password."
        );
      }

      onLogin(data);

    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">

      <div className="login-brand">

        <div className="logo">
          S
        </div>

        <div>
          <strong>
            SecureCert
          </strong>

          <span>
            Digital Certificate Platform
          </span>
        </div>

      </div>

      <div className="login-card">

        <div className="eyebrow">
          SECURE ACCESS
        </div>

        <h1>
          Welcome back
        </h1>

        <p>
          Sign in to manage and verify
          digital certificates.
        </p>

        <form onSubmit={login}>

          <label>
            Email
          </label>

          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) =>
              setEmail(event.target.value)
            }
            required
          />

          <label>
            Password
          </label>

          <input
            type="password"
            placeholder="Your password"
            value={password}
            onChange={(event) =>
              setPassword(event.target.value)
            }
            required
          />

          {error && (
            <div className="error">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="primary"
            disabled={loading}
          >
            {loading
              ? "Signing in..."
              : "Sign in"}
          </button>

        </form>

        <button
          className="text-button"
          onClick={() =>
            window.location.href = "/register"
          }
        >
          Create a student account →
        </button>

        <button
          className="text-button"
          onClick={() =>
            window.location.href =
              "/register/institution"
          }
        >
          Register an institution →
        </button>

        <button
          className="text-button"
          onClick={() =>
            window.location.href = "/verify"
          }
        >
          Verify a certificate →
        </button>

      </div>

      <div className="login-footer">
        Secure registration · JWT authentication · RSA verification
      </div>

    </div>
  );
}

/* =========================================================
   DASHBOARD
   ========================================================= */

function Dashboard({
  user,
  token,
  onLogout
}) {
  const [page, setPage] =
    useState("overview");

  function pageTitle() {

    if (page === "admin") {
      return "Institution Management";
    }

    if (page === "issuer") {
      return "Issue Certificate";
    }

    if (page === "student") {
      return "My Certificates";
    }

    return "Overview";
  }

  return (
    <div className="layout">

      <aside className="sidebar">

        <div className="brand">

          <div className="logo">
            S
          </div>

          <strong>
            SecureCert
          </strong>

        </div>

        <div className="role">
          {user.role}
        </div>

        <nav>

          <NavButton
            active={page === "overview"}
            onClick={() =>
              setPage("overview")
            }
          >
            Overview
          </NavButton>

          {user.role === "ADMIN" && (
            <NavButton
              active={page === "admin"}
              onClick={() =>
                setPage("admin")
              }
            >
              Institutions
            </NavButton>
          )}

          {user.role === "ISSUER" && (
            <NavButton
              active={page === "issuer"}
              onClick={() =>
                setPage("issuer")
              }
            >
              Issue Certificate
            </NavButton>
          )}

          {user.role === "STUDENT" && (
            <NavButton
              active={page === "student"}
              onClick={() =>
                setPage("student")
              }
            >
              My Certificates
            </NavButton>
          )}

        </nav>

        <div className="sidebar-bottom">

          <div className="mini-profile">

            <div className="avatar">
              {user.fullName?.charAt(0)}
            </div>

            <div>

              <strong>
                {user.fullName}
              </strong>

              <span>
                {user.email}
              </span>

            </div>

          </div>

          <button
            className="logout"
            onClick={onLogout}
          >
            Sign out
          </button>

        </div>

      </aside>

      <main className="main">

        <header className="header">

          <div>

            <span className="breadcrumb">
              SecureCert / Platform
            </span>

            <h1>
              {pageTitle()}
            </h1>

          </div>

          <span className="header-role">
            {user.role}
          </span>

        </header>

        {page === "overview" && (
          <Overview user={user} />
        )}

        {page === "admin" && (
          <AdminPage token={token} />
        )}

        {page === "issuer" && (
          <IssuerPage token={token} />
        )}

        {page === "student" && (
          <StudentPage
            user={user}
            token={token}
          />
        )}

      </main>

    </div>
  );
}

function NavButton({
  active,
  onClick,
  children
}) {
  return (
    <button
      className={
        active
          ? "nav active"
          : "nav"
      }
      onClick={onClick}
    >
      {children}
    </button>
  );
}

/* =========================================================
   OVERVIEW
   ========================================================= */

function Overview({ user }) {

  return (
    <div className="content">

      <section className="welcome">

        <div>

          <span className="eyebrow">
            DIGITAL CREDENTIAL PLATFORM
          </span>

          <h2>
            Welcome, {user.fullName}
          </h2>

          <p>
            Your SecureCert workspace is ready.
          </p>

        </div>

        <div className="security-badge">
          ✓
        </div>

      </section>

      <div className="stats">

        <Stat
          title="Account"
          value="Active"
        />

        <Stat
          title="Role"
          value={user.role}
        />

        <Stat
          title="Authentication"
          value="JWT"
        />

        <Stat
          title="Signatures"
          value="RSA"
        />

      </div>

      <section className="card">

        <span className="eyebrow">
          PLATFORM SECURITY
        </span>

        <h3>
          Certificate trust architecture
        </h3>

        <p>
          Certificates are protected using
          SHA-256 integrity hashing and
          RSA digital signatures.
        </p>

        <div className="flow">

          <span>
            Certificate
          </span>

          <b>→</b>

          <span>
            SHA-256
          </span>

          <b>→</b>

          <span>
            RSA Signature
          </span>

          <b>→</b>

          <span>
            Verification
          </span>

        </div>

      </section>

    </div>
  );
}

function Stat({
  title,
  value
}) {
  return (
    <div className="stat">

      <span>
        {title}
      </span>

      <strong>
        {value}
      </strong>

    </div>
  );
}

/* =========================================================
   ADMIN
   ========================================================= */

function AdminPage({ token }) {

  const [institutions, setInstitutions] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [message, setMessage] =
    useState("");

  useEffect(() => {

    async function loadInstitutions() {

      try {

        const response =
          await fetch(
            `${API}/api/institutions`,
            {
              headers: {
                Authorization:
                  `Bearer ${token}`
              }
            }
          );

        const data =
          await response.json();

        if (!response.ok) {

          throw new Error(
            data.message ||
            "Unable to load institutions."
          );
        }

        setInstitutions(
          Array.isArray(data)
            ? data
            : []
        );

      } catch (error) {

        setMessage(error.message);

      } finally {

        setLoading(false);

      }
    }

    loadInstitutions();

  }, [token]);

  async function activate(id) {

    setMessage("");

    try {

      const response =
        await fetch(
          `${API}/api/institutions/${id}/activate`,
          {
            method: "PATCH",

            headers: {
              Authorization:
                `Bearer ${token}`
            }
          }
        );

      const data =
        await response.json();

      if (!response.ok) {

        throw new Error(
          data.message ||
          "Unable to activate institution."
        );
      }

      setInstitutions((items) =>
        items.map((item) =>
          item.id === id
            ? data
            : item
        )
      );

      setMessage(
        "Institution activated successfully."
      );

    } catch (error) {

      setMessage(error.message);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              ADMINISTRATION
            </span>

            <h3>
              Institutions
            </h3>

            <p>
              Manage registered certificate
              issuing institutions.
            </p>

          </div>

        </div>

        {message && (
          <div
            className={
              message.includes(
                "successfully"
              )
                ? "success"
                : "error"
            }
          >
            {message}
          </div>
        )}

        {loading ? (

          <div className="empty">
            Loading institutions...
          </div>

        ) : institutions.length === 0 ? (

          <div className="empty">
            No institutions found.
          </div>

        ) : (

          <div className="table-container">

            <table>

              <thead>

                <tr>
                  <th>Institution</th>
                  <th>Registration</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>

              </thead>

              <tbody>

                {institutions.map(
                  (institution) => (

                    <tr
                      key={institution.id}
                    >

                      <td>

                        <strong>
                          {institution.name}
                        </strong>

                        <small>
                          {institution.email}
                        </small>

                      </td>

                      <td>
                        {
                          institution.registrationNumber
                        }
                      </td>

                      <td>

                        <span
                          className={
                            institution.status ===
                            "ACTIVE"
                              ? "status active"
                              : "status pending"
                          }
                        >
                          {institution.status}
                        </span>

                      </td>

                      <td>

                        {institution.status !==
                          "ACTIVE" && (

                          <button
                            className="small-button"
                            onClick={() =>
                              activate(
                                institution.id
                              )
                            }
                          >
                            Activate
                          </button>

                        )}

                      </td>

                    </tr>

                  )
                )}

              </tbody>

            </table>

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   ISSUER
   ========================================================= */

function IssuerPage({ token }) {

  const [students, setStudents] =
    useState([]);

  const [certificates, setCertificates] =
    useState([]);

  const [studentId, setStudentId] =
    useState("");

  const [courseName, setCourseName] =
    useState("");

  const [description, setDescription] =
    useState("");

  const [certificate, setCertificate] =
    useState(null);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const [studentsLoading, setStudentsLoading] =
    useState(true);

  const [certificatesLoading, setCertificatesLoading] =
    useState(true);

  useEffect(() => {

    async function loadStudents() {

      try {

        const response =
          await fetch(
            `${API}/api/issuer/students`,
            {
              headers: {
                Authorization:
                  `Bearer ${token}`
              }
            }
          );

        const data =
          await response.json();

        if (!response.ok) {

          throw new Error(
            data.message ||
            "Unable to load students."
          );
        }

        setStudents(
          Array.isArray(data)
            ? data
            : []
        );

        if (data.length > 0) {
          setStudentId(data[0].id);
        }

      } catch (error) {

        setError(error.message);

      } finally {

        setStudentsLoading(false);

      }
    }

    async function loadCertificates() {

      try {

        const response =
          await fetch(
            `${API}/api/certificates`,
            {
              headers: {
                Authorization:
                  `Bearer ${token}`
              }
            }
          );

        const data =
          await response.json();

        if (!response.ok) {

          throw new Error(
            data.message ||
            "Unable to load certificates."
          );
        }

        setCertificates(
          Array.isArray(data)
            ? data
            : []
        );

      } catch (error) {

        setError(error.message);

      } finally {

        setCertificatesLoading(false);

      }
    }

    loadStudents();
    loadCertificates();

  }, [token]);

  async function issue(event) {

    event.preventDefault();

    setLoading(true);
    setError("");
    setMessage("");
    setCertificate(null);

    try {

      const response =
        await fetch(
          `${API}/api/certificates`,
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
              courseName: courseName.trim(),
              description: description.trim()
            })
          }
        );

      const data =
        await response.json();

      if (!response.ok) {

        throw new Error(
          data.message ||
          "Unable to issue certificate."
        );
      }

      setCertificate(data);

      setCertificates((items) => [
        data,
        ...items
      ]);

      setCourseName("");
      setDescription("");

      setMessage(
        "Certificate issued successfully."
      );

    } catch (error) {

      setError(error.message);

    } finally {

      setLoading(false);

    }
  }

  async function revokeCertificate(
    certificateId
  ) {

    const reason =
      window.prompt(
        "Enter the reason for revocation:"
      );

    if (!reason || !reason.trim()) {
      return;
    }

    setError("");
    setMessage("");

    try {

      const response =
        await fetch(
          `${API}/api/certificates/${certificateId}/revoke`,
          {
            method: "PATCH",

            headers: {
              Authorization:
                `Bearer ${token}`,

              "Content-Type":
                "application/json"
            },

            body: JSON.stringify({
              reason: reason.trim()
            })
          }
        );

      const data =
        await response.json();

      if (!response.ok) {

        throw new Error(
          data.message ||
          "Unable to revoke certificate."
        );
      }

      setCertificates((items) =>
        items.map((item) =>
          item.id === data.id
            ? data
            : item
        )
      );

      if (
        certificate &&
        certificate.id === data.id
      ) {
        setCertificate(data);
      }

      setMessage(
        "Certificate revoked successfully."
      );

    } catch (error) {

      setError(error.message);

    }
  }

  async function downloadPdf(
    certificateId,
    certificateNumber
  ) {

    try {

      const response =
        await fetch(
          `${API}/api/certificates/${certificateId}/pdf`,
          {
            headers: {
              Authorization:
                `Bearer ${token}`
            }
          }
        );

      if (!response.ok) {

        throw new Error(
          "Unable to generate PDF."
        );
      }

      const blob =
        await response.blob();

      const url =
        URL.createObjectURL(blob);

      const link =
        document.createElement("a");

      link.href = url;

      link.download =
        `${certificateNumber}.pdf`;

      document.body.appendChild(link);

      link.click();

      link.remove();

      URL.revokeObjectURL(url);

    } catch (error) {

      setError(error.message);

    }
  }

  function verifyCertificate(
    certificateNumber
  ) {

    window.location.href =
      `/verify/${certificateNumber}`;
  }

  return (
    <div className="content">

      <div className="two-column">

        {/* ISSUE FORM */}

        <section className="card">

          <span className="eyebrow">
            CERTIFICATE ISSUANCE
          </span>

          <h3>
            Create certificate
          </h3>

          <p>
            Issue a digitally signed
            certificate to an active student.
          </p>

          <form
            className="form"
            onSubmit={issue}
          >

            <label>
              Student
            </label>

            {studentsLoading ? (

              <div className="loading-field">
                Loading students...
              </div>

            ) : students.length === 0 ? (

              <div className="error">
                No active students available.
              </div>

            ) : (

              <select
                value={studentId}
                onChange={(event) =>
                  setStudentId(
                    event.target.value
                  )
                }
                required
              >

                {students.map(
                  (student) => (

                    <option
                      key={student.id}
                      value={student.id}
                    >
                      {student.fullName}
                      {" — "}
                      {student.email}
                    </option>

                  )
                )}

              </select>

            )}

            <label>
              Course
            </label>

            <input
              type="text"
              placeholder="Java Full Stack Development"
              value={courseName}
              onChange={(event) =>
                setCourseName(
                  event.target.value
                )
              }
              required
            />

            <label>
              Description
            </label>

            <textarea
              rows="5"
              placeholder="Successfully completed the program."
              value={description}
              onChange={(event) =>
                setDescription(
                  event.target.value
                )
              }
            />

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

            <button
              type="submit"
              className="primary"
              disabled={
                loading ||
                studentsLoading ||
                students.length === 0
              }
            >
              {loading
                ? "Signing certificate..."
                : "Issue Certificate"}
            </button>

          </form>

        </section>

        {/* LATEST RESULT */}

        <section className="card">

          <span className="eyebrow">
            LATEST RESULT
          </span>

          {certificate ? (

            <>

              <div className="success-icon">
                ✓
              </div>

              <h3>
                Certificate issued
              </h3>

              <div className="certificate-number">
                {certificate.certificateNumber}
              </div>

              <div className="details">

                <Detail
                  label="Student"
                  value={
                    certificate.studentName
                  }
                />

                <Detail
                  label="Course"
                  value={
                    certificate.courseName
                  }
                />

                <Detail
                  label="Institution"
                  value={
                    certificate.institutionName
                  }
                />

                <Detail
                  label="Status"
                  value={
                    certificate.status
                  }
                />

                <Detail
                  label="Signature"
                  value={
                    certificate.signatureAlgorithm
                  }
                />

              </div>

              <div className="button-row">

                <button
                  className="secondary"
                  onClick={() =>
                    downloadPdf(
                      certificate.id,
                      certificate.certificateNumber
                    )
                  }
                >
                  Download PDF
                </button>

                <button
                  className="small-button"
                  onClick={() =>
                    verifyCertificate(
                      certificate.certificateNumber
                    )
                  }
                >
                  Verify
                </button>

              </div>

            </>

          ) : (

            <div className="empty">

              <div className="empty-icon">
                ◇
              </div>

              <p>
                Your latest signed certificate
                will appear here.
              </p>

            </div>

          )}

        </section>

      </div>

      {/* CERTIFICATE MANAGEMENT */}

      <section className="card certificate-management">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              CERTIFICATE MANAGEMENT
            </span>

            <h3>
              Issued Certificates
            </h3>

            <p>
              View, verify, download and revoke
              certificates issued by your institution.
            </p>

          </div>

          <strong>
            {certificates.length}
          </strong>

        </div>

        {certificatesLoading ? (

          <div className="empty">
            Loading certificates...
          </div>

        ) : certificates.length === 0 ? (

          <div className="empty">
            No certificates have been issued yet.
          </div>

        ) : (

          <div className="certificate-list">

            {certificates.map(
              (item) => (

                <div
                  className="certificate-item"
                  key={item.id}
                >

                  <div className="certificate-main">

                    <div className="certificate-symbol">
                      {item.status === "ACTIVE"
                        ? "✓"
                        : "!"}
                    </div>

                    <div>

                      <span className="certificate-number">
                        {item.certificateNumber}
                      </span>

                      <h3>
                        {item.courseName}
                      </h3>

                      <p>
                        {item.studentName}
                        {" · "}
                        {item.institutionName}
                      </p>

                      <small>
                        Issued on{" "}
                        {item.issueDate}
                      </small>

                    </div>

                  </div>

                  <div className="certificate-actions">

                    <span
                      className={
                        item.status === "ACTIVE"
                          ? "status active"
                          : "status revoked"
                      }
                    >
                      {item.status}
                    </span>

                    <button
                      className="small-button"
                      onClick={() =>
                        downloadPdf(
                          item.id,
                          item.certificateNumber
                        )
                      }
                    >
                      PDF
                    </button>

                    <button
                      className="small-button"
                      onClick={() =>
                        verifyCertificate(
                          item.certificateNumber
                        )
                      }
                    >
                      Verify
                    </button>

                    {item.status === "ACTIVE" && (

                      <button
                        className="danger-button"
                        onClick={() =>
                          revokeCertificate(
                            item.id
                          )
                        }
                      >
                        Revoke
                      </button>

                    )}

                  </div>

                </div>

              )
            )}

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   STUDENT
   ========================================================= */

function StudentPage({
  user,
  token
}) {

  const [certificates, setCertificates] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {

    async function loadCertificates() {

      try {

        const response =
          await fetch(
            `${API}/api/student/certificates`,
            {
              headers: {
                Authorization:
                  `Bearer ${token}`
              }
            }
          );

        const data =
          await response.json();

        if (!response.ok) {

          throw new Error(
            data.message ||
            "Unable to load certificates."
          );
        }

        setCertificates(
          Array.isArray(data)
            ? data
            : []
        );

      } catch (error) {

        setError(error.message);

      } finally {

        setLoading(false);

      }
    }

    loadCertificates();

  }, [token]);

  async function downloadPdf(
    certificateId,
    certificateNumber
  ) {

    try {

      const response =
        await fetch(
          `${API}/api/student/certificates/${certificateId}/pdf`,
          {
            headers: {
              Authorization:
                `Bearer ${token}`
            }
          }
        );

      if (!response.ok) {

        throw new Error(
          "Unable to download certificate."
        );
      }

      const blob =
        await response.blob();

      const url =
        URL.createObjectURL(blob);

      const link =
        document.createElement("a");

      link.href = url;

      link.download =
        `${certificateNumber}.pdf`;

      document.body.appendChild(link);

      link.click();

      link.remove();

      URL.revokeObjectURL(url);

    } catch (error) {

      setError(error.message);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <span className="eyebrow">
          STUDENT PROFILE
        </span>

        <div className="student-profile">

          <div className="large-avatar">
            {user.fullName?.charAt(0)}
          </div>

          <div>

            <h2>
              {user.fullName}
            </h2>

            <p>
              {user.email}
            </p>

            <span className="status active">
              VERIFIED STUDENT
            </span>

          </div>

        </div>

      </section>

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              DIGITAL CREDENTIALS
            </span>

            <h3>
              My Certificates
            </h3>

            <p>
              Certificates issued to your
              verified student account.
            </p>

          </div>

          <strong>
            {certificates.length}
          </strong>

        </div>

        {loading && (
          <div className="empty">
            Loading certificates...
          </div>
        )}

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {!loading &&
          !error &&
          certificates.length === 0 && (

            <div className="empty">

              <div className="empty-icon">
                ◇
              </div>

              <p>
                No certificates have been
                issued to you yet.
              </p>

            </div>

          )}

        {!loading &&
          certificates.length > 0 && (

            <div className="certificate-list">

              {certificates.map(
                (certificate) => (

                  <div
                    className="certificate-item"
                    key={certificate.id}
                  >

                    <div className="certificate-main">

                      <div className="certificate-symbol">
                        {certificate.status ===
                        "ACTIVE"
                          ? "✓"
                          : "!"}
                      </div>

                      <div>

                        <span className="certificate-number">
                          {certificate.certificateNumber}
                        </span>

                        <h3>
                          {certificate.courseName}
                        </h3>

                        <p>
                          {certificate.institutionName}
                        </p>

                        <small>
                          Issued on{" "}
                          {certificate.issueDate}
                        </small>

                      </div>

                    </div>

                    <div className="certificate-actions">

                      <span
                        className={
                          certificate.status ===
                          "ACTIVE"
                            ? "status active"
                            : "status revoked"
                        }
                      >
                        {certificate.status}
                      </span>

                      <button
                        className="secondary"
                        onClick={() =>
                          downloadPdf(
                            certificate.id,
                            certificate.certificateNumber
                          )
                        }
                      >
                        Download PDF
                      </button>

                      <button
                        className="small-button"
                        onClick={() =>
                          window.location.href =
                            `/verify/${certificate.certificateNumber}`
                        }
                      >
                        Verify
                      </button>

                    </div>

                  </div>

                )
              )}

            </div>

          )}

      </section>

    </div>
  );
}

/* =========================================================
   PUBLIC VERIFICATION
   ========================================================= */

function VerifyPage({
  initialNumber,
  onBack
}) {

  const [number, setNumber] =
    useState(initialNumber);

  const [result, setResult] =
    useState(null);

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  async function verify(event) {

    event.preventDefault();

    setLoading(true);
    setError("");
    setResult(null);

    try {

      const cleanNumber =
        number.trim();

      const response =
        await fetch(
          `${API}/api/public/certificates/${encodeURIComponent(
            cleanNumber
          )}/verify`
        );

      const data =
        await response.json();

      if (!response.ok) {

        throw new Error(
          data.message ||
          "Certificate not found."
        );
      }

      setResult(data);

    } catch (error) {

      setError(error.message);

    } finally {

      setLoading(false);

    }
  }

  const valid =
    result?.valid === true;

  return (
    <div className="verify-page">

      <header className="verify-header">

        <div
          className="brand"
          onClick={onBack}
        >

          <div className="logo">
            S
          </div>

          <strong>
            SecureCert
          </strong>

        </div>

        <span>
          Public Verification
        </span>

      </header>

      <main className="verify-main">

        <span className="eyebrow">
          TRUSTED CREDENTIALS
        </span>

        <h1>
          Verify a certificate
        </h1>

        <p>
          Enter a certificate number to
          verify its authenticity and
          current status.
        </p>

        <form
          className="verify-form"
          onSubmit={verify}
        >

          <input
            value={number}
            onChange={(event) =>
              setNumber(
                event.target.value
              )
            }
            placeholder="SC-2026-000001"
            required
          />

          <button
            type="submit"
            className="primary"
            disabled={loading}
          >
            {loading
              ? "Verifying..."
              : "Verify"}
          </button>

        </form>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {result && (

          <section
            className={
              valid
                ? "verification valid"
                : "verification invalid"
            }
          >

            <div className="verification-top">

              <div className="verification-icon">
                {valid ? "✓" : "!"}
              </div>

              <div>

                <span>
                  {valid
                    ? "CERTIFICATE AUTHENTIC"
                    : result.status === "REVOKED"
                      ? "CERTIFICATE REVOKED"
                      : "CERTIFICATE INVALID"}
                </span>

                <h2>
                  {result.studentName}
                </h2>

                <p>
                  {result.courseName}
                </p>

              </div>

            </div>

            <div className="verification-details">

              <Detail
                label="Certificate"
                value={
                  result.certificateNumber
                }
              />

              <Detail
                label="Institution"
                value={
                  result.institutionName
                }
              />

              <Detail
                label="Issuer"
                value={
                  result.issuerName
                }
              />

              <Detail
                label="Issue Date"
                value={
                  result.issueDate
                }
              />

              <Detail
                label="Integrity"
                value={
                  result.integrityValid
                    ? "Verified"
                    : "Failed"
                }
              />

              <Detail
                label="Digital Signature"
                value={
                  result.signatureValid
                    ? "Verified"
                    : "Failed"
                }
              />

              <Detail
                label="Status"
                value={
                  result.status
                }
              />

            </div>

            <div className="verification-message">
              {result.message}
            </div>

          </section>

        )}

      </main>

    </div>
  );
}

/* =========================================================
   SHARED DETAIL
   ========================================================= */

function Detail({
  label,
  value
}) {
  return (
    <div className="detail">

      <span>
        {label}
      </span>

      <strong>
        {value}
      </strong>

    </div>
  );
}

export default App;