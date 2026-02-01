import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

function Landing() {
    // Calculator state
    const [calcAmount, setCalcAmount] = useState(5000);
    const [calcMonths, setCalcMonths] = useState(12);
    const [calcRate, setCalcRate] = useState(12.0);
    const [monthlyPayment, setMonthlyPayment] = useState(0);

    useEffect(() => {
        // Dynamic interest rate calculation logic:
        // Base rate is 24%. Lower amount/term = slightly higher rate.
        // Higher amount/term = lower rate.
        let rate = 24 - (calcAmount / 10000) - (calcMonths / 12) * 1.2;
        rate = Math.max(10.9, Math.min(31.9, rate));
        setCalcRate(parseFloat(rate.toFixed(1)));

        const monthlyRate = rate / 100 / 12;
        const payment = (calcAmount * monthlyRate * Math.pow(1 + monthlyRate, calcMonths)) / (Math.pow(1 + monthlyRate, calcMonths) - 1);
        setMonthlyPayment(Math.round(payment));
    }, [calcAmount, calcMonths]);

    return (
        <>
            <section className="hero">
                <div className="hero-content">
                    <div className="hero-text">
                        <h1>Sürətli və Asan Onlayn Kredit</h1>
                        <p>
                            Credoline ilə kredit almaq heç vaxt bu qədər asan olmamışdı.
                            Müraciətinizi tam onlayn şəkildə tamamlayın.
                        </p>
                        <div className="hero-features">
                            <div className="hero-feature">
                                <span className="hero-feature-icon">⚡</span>
                                <span>5 dəqiqə ərzində cavab</span>
                            </div>
                            <div className="hero-feature">
                                <span className="hero-feature-icon">📱</span>
                                <span>Tam onlayn proses</span>
                            </div>
                            <div className="hero-feature">
                                <span className="hero-feature-icon">🔒</span>
                                <span>Təhlükəsiz və gizli</span>
                            </div>
                            <div className="hero-feature">
                                <span className="hero-feature-icon">💰</span>
                                <span>100 - 50,000 AZN</span>
                            </div>
                        </div>
                        <Link to="/apply" className="btn btn-primary btn-lg">
                            İndi Müraciət Et →
                        </Link>
                    </div>

                    <div className="hero-stats">
                        <h3>Niyə Credoline?</h3>
                        <div className="stats-grid">
                            <div className="stat-item">
                                <div className="stat-value">10,9%</div>
                                <div className="stat-label">-dən başlayan illik faiz</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-value">59</div>
                                <div className="stat-label">ay-a qədər müddət</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-value">50K+</div>
                                <div className="stat-label">razı müştəri</div>
                            </div>
                            <div className="stat-item">
                                <div className="stat-value">24/7</div>
                                <div className="stat-label">onlayn dəstək</div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Credit Calculator Section */}
            <section style={{ padding: '4rem 2rem', background: 'var(--gray-50)' }}>
                <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
                    <h2 style={{ textAlign: 'center', marginBottom: '3rem', fontSize: '2rem', color: 'var(--gray-900)' }}>
                        Kredit Kalkulyatoru
                    </h2>
                    <div className="card" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '3rem', padding: '2.5rem' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
                            <div className="form-group">
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                                    <label className="form-label" style={{ marginBottom: 0 }}>Məbləğ (AZN)</label>
                                    <span style={{ fontWeight: 600, color: 'var(--primary-600)' }}>{calcAmount.toLocaleString()} AZN</span>
                                </div>
                                <input
                                    type="range"
                                    className="slider"
                                    min={500}
                                    max={50000}
                                    step={500}
                                    value={calcAmount}
                                    onChange={(e) => setCalcAmount(Number(e.target.value))}
                                />
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.5rem' }}>
                                    <span>500 AZN</span>
                                    <span>50 000 AZN</span>
                                </div>
                            </div>

                            <div className="form-group">
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                                    <label className="form-label" style={{ marginBottom: 0 }}>Müddət (ay)</label>
                                    <span style={{ fontWeight: 600, color: 'var(--primary-600)' }}>{calcMonths} ay</span>
                                </div>
                                <input
                                    type="range"
                                    className="slider"
                                    min={3}
                                    max={59}
                                    step={1}
                                    value={calcMonths}
                                    onChange={(e) => setCalcMonths(Number(e.target.value))}
                                />
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.5rem' }}>
                                    <span>3 ay</span>
                                    <span>59 ay</span>
                                </div>
                            </div>

                            <div style={{
                                background: 'white',
                                borderRadius: 'var(--radius)',
                                padding: '1.25rem',
                                border: '1px solid var(--gray-200)',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center'
                            }}>
                                <span style={{ color: 'var(--gray-500)' }}>İllik faiz dərəcəsi</span>
                                <span style={{ fontWeight: 700, color: 'var(--primary-600)', fontSize: '1.25rem' }}>{calcRate}%</span>
                            </div>
                        </div>

                        <div style={{
                            background: 'var(--primary-600)',
                            color: 'white',
                            borderRadius: 'var(--radius)',
                            padding: '2rem',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'center',
                            alignItems: 'center',
                            textAlign: 'center'
                        }}>
                            <p style={{ opacity: 0.9, marginBottom: '0.5rem', fontSize: '1.125rem' }}>Aylıq ödəniş</p>
                            <h3 style={{ fontSize: '3rem', marginBottom: '2rem' }}>{monthlyPayment.toLocaleString()} AZN</h3>

                            <div style={{ width: '100%', paddingTop: '1.5rem', borderTop: '1px solid rgba(255,255,255,0.2)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <span style={{ opacity: 0.8 }}>Cəmi ödəniş</span>
                                    <span style={{ fontWeight: 600 }}>{(monthlyPayment * calcMonths).toLocaleString()} AZN</span>
                                </div>
                            </div>

                            <Link to="/apply" className="btn btn-secondary" style={{ marginTop: '2.5rem', width: '100%', background: 'white', color: 'var(--primary-600)' }}>
                                İndi Müraciət Et
                            </Link>
                        </div>
                    </div>
                </div>
            </section>

            <section style={{ padding: '4rem 2rem', maxWidth: '1280px', margin: '0 auto' }}>
                <h2 style={{ textAlign: 'center', marginBottom: '3rem', fontSize: '2rem', color: 'var(--gray-900)' }}>
                    4 Addımda Kredit Alın
                </h2>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2rem' }}>
                    <div className="card">
                        <div style={{
                            width: '48px',
                            height: '48px',
                            background: 'var(--primary-100)',
                            borderRadius: 'var(--radius-full)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '1.5rem',
                            marginBottom: '1rem'
                        }}>
                            📱
                        </div>
                        <h3 style={{ marginBottom: '0.5rem' }}>1. Mobil nömrənin təsdiqi</h3>
                        <p style={{ color: 'var(--gray-500)', fontSize: '0.875rem' }}>
                            Mobil nömrənizi daxil edin və SMS ilə göndərilən kodu təsdiq edin.
                        </p>
                    </div>

                    <div className="card">
                        <div style={{
                            width: '48px',
                            height: '48px',
                            background: 'var(--primary-100)',
                            borderRadius: 'var(--radius-full)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '1.5rem',
                            marginBottom: '1rem'
                        }}>
                            👤
                        </div>
                        <h3 style={{ marginBottom: '0.5rem' }}>2. Şəxsi Məlumatlar</h3>
                        <p style={{ color: 'var(--gray-500)', fontSize: '0.875rem' }}>
                            ASAN sistemindən avtomatik yüklənən şəxsi və maliyyə məlumatlarınızı yoxlayın.
                        </p>
                    </div>

                    <div className="card">
                        <div style={{
                            width: '48px',
                            height: '48px',
                            background: 'var(--primary-100)',
                            borderRadius: 'var(--radius-full)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '1.5rem',
                            marginBottom: '1rem'
                        }}>
                            💵
                        </div>
                        <h3 style={{ marginBottom: '0.5rem' }}>3. Məbləğ Seçimi</h3>
                        <p style={{ color: 'var(--gray-500)', fontSize: '0.875rem' }}>
                            İstədiyiniz kredit məbləğini və ödəniş müddətini seçin.
                        </p>
                    </div>

                    <div className="card">
                        <div style={{
                            width: '48px',
                            height: '48px',
                            background: 'var(--success-50)',
                            borderRadius: 'var(--radius-full)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '1.5rem',
                            marginBottom: '1rem'
                        }}>
                            ✓
                        </div>
                        <h3 style={{ marginBottom: '0.5rem' }}>4. Nəticə</h3>
                        <p style={{ color: 'var(--gray-500)', fontSize: '0.875rem' }}>
                            Anında qərar alın - təsdiq, rədd və ya əlavə baxış.
                        </p>
                    </div>
                </div>
            </section>
        </>
    );
}

export default Landing;
