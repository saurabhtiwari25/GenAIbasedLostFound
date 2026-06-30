import React from 'react';

const ItemCard = ({ item, onClick }) => {
    return (
        <div onClick={onClick} className="item-card-hover">
            <div className="item-card-image-wrap">
                {item.imagePath ? (
                    <img
                        src={item.imagePath}
                        alt={item.title}
                    />
                ) : (
                    <div className="item-card-placeholder">
                        <span className="icon"></span>
                        <span className="label">No Image Attached</span>
                    </div>
                )}
                <div className={`item-card-status-badge ${item.found ? 'found' : 'lost'}`}>
                    {item.found ? " Found" : " Lost"}
                </div>
            </div>

            <div className="item-card-body">
                <h3>{item.title}</h3>
                {item.reporterName && (
                    <div className="item-card-reporter">
                        <span> Reported by — <strong>{item.reporterName}</strong></span>
                    </div>
                )}
                <p className="item-card-desc">{item.description}</p>
                <div className="item-card-footer">
                    <p>
                        Place: <span>{item.location}</span>
                    </p>
                    <span>
                        {new Date(item.createdAt).toLocaleDateString()}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default ItemCard;
