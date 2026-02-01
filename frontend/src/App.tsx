import { Routes, Route } from 'react-router-dom'
import Landing from './pages/Landing'
import ApplyWizard from './pages/ApplyWizard'
import TrackApplication from './pages/TrackApplication'
import Layout from './components/Layout'

function App() {
    return (
        <Routes>
            <Route path="/" element={<Layout />}>
                <Route index element={<Landing />} />
                <Route path="apply" element={<ApplyWizard />} />
                <Route path="track" element={<TrackApplication />} />
            </Route>
        </Routes>
    )
}

export default App
