import { Outlet, Link, useLocation } from 'react-router-dom';

function Layout() {
    const location = useLocation();

    return (
        <div className="layout">
            <header className="header">
                <div className="header-content">
                    <Link to="/" className="logo">
                        <div className="logo-icon">C</div>
                        <span>Credoline</span>
                    </Link>
                    <nav className="nav-links">
                        <Link
                            to="/"
                            className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
                        >
                            Ana səhifə
                        </Link>
                        <Link
                            to="/apply"
                            className={`nav-link ${location.pathname === '/apply' ? 'active' : ''}`}
                        >
                            Kredit al
                        </Link>
                    </nav>
                </div>
            </header>

            <main className="main-content">
                <Outlet />
            </main>

            <footer className="footer">
                <div className="footer-content">
                    <p className="footer-text">
                        © 2026 Credoline. Bütün hüquqlar qorunur. |
                        Lisenziya: Mərkəzi Bank #123
                    </p>
                </div>
            </footer>
        </div>
    );
}

export default Layout;
