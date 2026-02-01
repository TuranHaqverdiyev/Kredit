interface ProgressStepperProps {
    currentStep: number;
    steps: string[];
}

function ProgressStepper({ currentStep, steps }: ProgressStepperProps) {
    return (
        <div className="stepper">
            {steps.map((step, index) => {
                const stepNumber = index + 1;
                const isCompleted = stepNumber < currentStep;
                const isActive = stepNumber === currentStep;

                return (
                    <div
                        key={step}
                        className={`step ${isCompleted ? 'completed' : ''} ${isActive ? 'active' : ''}`}
                    >
                        <div className="step-circle">
                            {isCompleted ? '✓' : stepNumber}
                        </div>
                        {index < steps.length - 1 && <div className="step-line" />}
                    </div>
                );
            })}
        </div>
    );
}

export default ProgressStepper;
